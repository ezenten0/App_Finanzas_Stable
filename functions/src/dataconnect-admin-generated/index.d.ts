import { ConnectorConfig, DataConnect, OperationOptions, ExecuteOperationResponse } from 'firebase-admin/data-connect';

export const connectorConfig: ConnectorConfig;

export type TimestampString = string;
export type UUIDString = string;
export type Int64String = string;
export type DateString = string;


export interface Account_Key {
  id: UUIDString;
  __typename?: 'Account_Key';
}

export interface Budget_Key {
  id: UUIDString;
  __typename?: 'Budget_Key';
}

export interface Category_Key {
  id: UUIDString;
  __typename?: 'Category_Key';
}

export interface CreateTransactionData {
  transaction_insert: Transaction_Key;
}

export interface CreateTransactionVariables {
  accountId: UUIDString;
  amount: number;
  date: TimestampString;
  description: string;
  type: string;
  categoryId?: UUIDString | null;
}

export interface CreateUserData {
  user_insert: User_Key;
}

export interface CreateUserVariables {
  displayName: string;
  email: string;
}

export interface GetBudgetForCategoryData {
  budgets: ({
    id: UUIDString;
    amount: number;
    startDate: TimestampString;
    endDate?: TimestampString | null;
    period: string;
  } & Budget_Key)[];
}

export interface GetBudgetForCategoryVariables {
  categoryId: UUIDString;
}

export interface GetTransactionsForAccountData {
  transactions: ({
    id: UUIDString;
    amount: number;
    date: TimestampString;
    description?: string | null;
    type: string;
    category?: {
      id: UUIDString;
      name: string;
    } & Category_Key;
  } & Transaction_Key)[];
}

export interface GetTransactionsForAccountVariables {
  accountId: UUIDString;
}

export interface Goal_Key {
  id: UUIDString;
  __typename?: 'Goal_Key';
}

export interface Transaction_Key {
  id: UUIDString;
  __typename?: 'Transaction_Key';
}

export interface User_Key {
  id: UUIDString;
  __typename?: 'User_Key';
}

/** Generated Node Admin SDK operation action function for the 'CreateUser' Mutation. Allow users to execute without passing in DataConnect. */
export function createUser(dc: DataConnect, vars: CreateUserVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<CreateUserData>>;
/** Generated Node Admin SDK operation action function for the 'CreateUser' Mutation. Allow users to pass in custom DataConnect instances. */
export function createUser(vars: CreateUserVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<CreateUserData>>;

/** Generated Node Admin SDK operation action function for the 'GetTransactionsForAccount' Query. Allow users to execute without passing in DataConnect. */
export function getTransactionsForAccount(dc: DataConnect, vars: GetTransactionsForAccountVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<GetTransactionsForAccountData>>;
/** Generated Node Admin SDK operation action function for the 'GetTransactionsForAccount' Query. Allow users to pass in custom DataConnect instances. */
export function getTransactionsForAccount(vars: GetTransactionsForAccountVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<GetTransactionsForAccountData>>;

/** Generated Node Admin SDK operation action function for the 'CreateTransaction' Mutation. Allow users to execute without passing in DataConnect. */
export function createTransaction(dc: DataConnect, vars: CreateTransactionVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<CreateTransactionData>>;
/** Generated Node Admin SDK operation action function for the 'CreateTransaction' Mutation. Allow users to pass in custom DataConnect instances. */
export function createTransaction(vars: CreateTransactionVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<CreateTransactionData>>;

/** Generated Node Admin SDK operation action function for the 'GetBudgetForCategory' Query. Allow users to execute without passing in DataConnect. */
export function getBudgetForCategory(dc: DataConnect, vars: GetBudgetForCategoryVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<GetBudgetForCategoryData>>;
/** Generated Node Admin SDK operation action function for the 'GetBudgetForCategory' Query. Allow users to pass in custom DataConnect instances. */
export function getBudgetForCategory(vars: GetBudgetForCategoryVariables, options?: OperationOptions): Promise<ExecuteOperationResponse<GetBudgetForCategoryData>>;

