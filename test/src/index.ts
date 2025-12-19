import { setGlobalOptions } from "firebase-functions/v2";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import { initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

setGlobalOptions({ maxInstances: 10 });

initializeApp();
const db = getFirestore();

type TxDoc = {
  id?: number;
  title?: string;
  description?: string;
  amount?: number;       // tu app guarda Double
  amountCents?: number;  // si migras a centavos
  type?: string;         // INCOME/EXPENSE o CREDIT/DEBIT
  category?: string;
  date?: string;         // YYYY-MM-DD
  monthKey?: string;     // YYYY-MM
};

function monthKeyFrom(tx: TxDoc): string {
  if (tx.monthKey && tx.monthKey.trim().length > 0) return tx.monthKey;
  const d = (tx.date ?? "").trim();
  if (d.length >= 7) return d.substring(0, 7);
  return "unknown";
}

function toCents(tx: TxDoc): number {
  if (typeof tx.amountCents === "number") return Math.trunc(tx.amountCents);
  const a = typeof tx.amount === "number" ? tx.amount : 0;
  return Math.round(a * 100);
}

function isIncome(tx: TxDoc): boolean {
  const t = (tx.type ?? "").toUpperCase();
  return t === "INCOME" || t === "CREDIT";
}

function isExpense(tx: TxDoc): boolean {
  const t = (tx.type ?? "").toUpperCase();
  return t === "EXPENSE" || t === "DEBIT";
}

function safeCategory(tx: TxDoc): string {
  const c = (tx.category ?? "").trim();
  return c.length > 0 ? c : "Sin categoría";
}

/**
 * Trigger (microservicio en Firebase):
 * Cuando se crea/actualiza/elimina una transacción, actualiza insights por mes.
 * Guarda: users/{uid}/insights/monthly_{YYYY-MM}
 */
export const onTransactionWritten = onDocumentWritten(
  {
    region: "southamerica-west1",
    document: "users/{uid}/transactions/{txId}",
  },
  async (event) => {
    const uid = event.params.uid as string;

    const before = event.data?.before?.exists ? (event.data.before.data() as TxDoc) : null;
    const after = event.data?.after?.exists ? (event.data.after.data() as TxDoc) : null;

    if (!before && !after) return;

    const byMonth = new Map<string, Array<{ sign: number; tx: TxDoc }>>();

    const pushDelta = (doc: TxDoc, sign: number) => {
      const mk = monthKeyFrom(doc);
      const list = byMonth.get(mk) ?? [];
      list.push({ sign, tx: doc });
      byMonth.set(mk, list);
    };

    if (before) pushDelta(before, -1);
    if (after) pushDelta(after, +1);

    for (const [monthKey, deltas] of byMonth.entries()) {
      const insightRef = db
        .collection("users")
        .doc(uid)
        .collection("insights")
        .doc(`monthly_${monthKey}`);

      await db.runTransaction(async (tx) => {
        const snap = await tx.get(insightRef);
        const base = snap.exists ? (snap.data() as any) : {};

        let totalIncomeCents = Number(base.totalIncomeCents ?? 0);
        let totalExpenseCents = Number(base.totalExpenseCents ?? 0);

        const expensesByCategory: Record<string, number> = { ...(base.expensesByCategory ?? {}) };
        const incomesByCategory: Record<string, number> = { ...(base.incomesByCategory ?? {}) };

        for (const { sign, tx: doc } of deltas) {
          const cents = toCents(doc);
          const cat = safeCategory(doc);

          if (isIncome(doc)) {
            totalIncomeCents += sign * cents;
            incomesByCategory[cat] = Number(incomesByCategory[cat] ?? 0) + sign * cents;
          } else if (isExpense(doc)) {
            totalExpenseCents += sign * cents;
            expensesByCategory[cat] = Number(expensesByCategory[cat] ?? 0) + sign * cents;
          } else {
            // Si type es desconocido: lo tratamos como gasto (seguro)
            totalExpenseCents += sign * cents;
            expensesByCategory[cat] = Number(expensesByCategory[cat] ?? 0) + sign * cents;
          }
        }

        totalIncomeCents = Math.max(0, totalIncomeCents);
        totalExpenseCents = Math.max(0, totalExpenseCents);

        tx.set(
          insightRef,
          {
            monthKey,
            totalIncomeCents,
            totalExpenseCents,
            netBalanceCents: totalIncomeCents - totalExpenseCents,
            incomesByCategory,
            expensesByCategory,
            updatedAt: FieldValue.serverTimestamp(),
          },
          { merge: true }
        );
      });

      logger.info("Insights actualizados", { uid, monthKey });
    }
  }
);
