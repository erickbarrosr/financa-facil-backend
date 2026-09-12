# Frontend — Plano de Integração com o Backend

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrar o frontend React de dados mockados (Zustand local) para consumir a API REST do backend, substituindo o store de dados por React Query + cliente HTTP com refresh automático de token.

**Architecture:** O cliente HTTP (axios) injeta o Bearer token via interceptor de request. Ao receber 401, chama `/auth/refresh` e reenvia. O access token fica em memória (variável de módulo). O Zustand mantém apenas estado de UI (modal aberto, filtros). React Query gerencia cache, loading e erro dos dados remotos.

**Tech Stack:** React 19, TanStack Query 5, Axios, TanStack Router, Zustand (apenas UI state), TypeScript.

## Global Constraints

- Access token armazenado em memória JS (não `localStorage`, não `sessionStorage`)
- Refresh token em cookie HttpOnly (transparente — gerenciado pelo browser)
- `BASE_URL` configurado via variável de ambiente Vite (`VITE_API_URL`)
- O campo `paid: boolean` do frontend é substituído por `status: string` (PENDING | PAID | OVERDUE | CANCELLED)
- Todos os tipos de dados vêm da API — remover tipos hardcoded do frontend onde divergirem
- Nunca usar `any` — todos os tipos devem ser explícitos
- Working directory: `/home/erezende/financa-facil/financa-facil-frontend`

---

### Task 16: Cliente HTTP e gerenciamento de token

**Files:**
- Create: `src/lib/api-client.ts`
- Create: `src/lib/token-store.ts`
- Modify: `src/types/finance.ts`
- Create: `src/lib/query-client.ts`
- Modify: `vite.config.ts` (adicionar VITE_API_URL)

**Interfaces:**
- Produces:
  - `apiClient` — instância Axios configurada com interceptors
  - `tokenStore` — `getAccessToken(): string | null`, `setAccessToken(token: string): void`, `clearAccessToken(): void`
  - `queryClient` — instância TanStack Query para uso global

- [ ] **Step 1: Adicionar axios ao projeto**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
npm install axios
```

Esperado: `axios` adicionado ao `package.json`.

- [ ] **Step 2: Criar `src/lib/token-store.ts`**

```typescript
let accessToken: string | null = null;

export const tokenStore = {
  getAccessToken: () => accessToken,
  setAccessToken: (token: string) => { accessToken = token; },
  clearAccessToken: () => { accessToken = null; },
};
```

- [ ] **Step 3: Criar `src/lib/api-client.ts`**

```typescript
import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios';
import { tokenStore } from './token-store';

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken();
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (value: unknown) => void; reject: (reason?: unknown) => void }> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) { reject(error); } else { resolve(token); }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${token}` };
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const { data } = await axios.post(`${BASE_URL}/auth/refresh`, {}, { withCredentials: true });
        const newToken: string = data.data.accessToken;
        tokenStore.setAccessToken(newToken);
        processQueue(null, newToken);
        originalRequest.headers = { ...originalRequest.headers, Authorization: `Bearer ${newToken}` };
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        tokenStore.clearAccessToken();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);
```

- [ ] **Step 4: Criar `src/lib/query-client.ts`**

```typescript
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
      refetchOnWindowFocus: false,
    },
  },
});
```

- [ ] **Step 5: Adicionar `QueryClientProvider` e `VITE_API_URL` ao projeto**

Modifique `src/__root.tsx` (ou o arquivo raiz do TanStack Router) para envolver a app com `QueryClientProvider`:

```typescript
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from '@/lib/query-client';

// Dentro do componente raiz:
return (
  <QueryClientProvider client={queryClient}>
    {/* ... outlet existente ... */}
  </QueryClientProvider>
);
```

Crie `.env.development` na raiz do frontend:
```env
VITE_API_URL=http://localhost:8080/api/v1
```

Crie `.env.production` na raiz do frontend:
```env
VITE_API_URL=https://seu-backend.railway.app/api/v1
```

- [ ] **Step 6: Atualizar `src/types/finance.ts`**

```typescript
export type TransactionType = 'income' | 'expense';
export type TransactionStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';
export type AccountType = 'checking' | 'wallet' | 'credit_card' | 'investment';

export interface Account {
  id: string;
  name: string;
  type: AccountType;
  balance: number;
  color: string;
  createdAt: string;
}

export interface Category {
  id: string;
  name: string;
  type: TransactionType;
  color: string;
  icon: string;
}

export interface Transaction {
  id: string;
  type: TransactionType;
  amount: number;
  categoryId: string | null;
  accountId: string;
  transactionDate: string;
  description?: string;
  status: TransactionStatus;
}

export interface User {
  id: string;
  name: string;
  email: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: { code: string; message: string };
  timestamp: string;
}
```

- [ ] **Step 7: Verificar que o projeto compila sem erros**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
npm run build 2>&1 | tail -20
```

Esperado: `build successful` (pode haver erros de tipo nos componentes que ainda usam `paid: boolean` — serão corrigidos nas próximas tasks).

- [ ] **Step 8: Commit**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
git add src/lib/ src/types/ .env.development .env.production
git commit -m "feat: add axios api client with jwt interceptor and react query setup"
```

---

### Task 17: Migrar autenticação para API

**Files:**
- Modify: `src/store/finance.ts`
- Create: `src/hooks/use-auth.ts`
- Modify: `src/routes/login.tsx`
- Modify: `src/routes/signup.tsx`
- Modify: `src/routes/forgot-password.tsx`

**Interfaces:**
- Consumes: `apiClient` (Task 16), `tokenStore` (Task 16)
- Produces:
  - `useAuth()` — `{ user, isAuthenticated, login, logout, signup }`

- [ ] **Step 1: Criar `src/hooks/use-auth.ts`**

```typescript
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { tokenStore } from '@/lib/token-store';
import type { AuthResponse, User, ApiResponse } from '@/types/finance';
import { useFinanceUI } from '@/store/finance';

export function useAuth() {
  const queryClient = useQueryClient();

  const { data: user, isLoading } = useQuery<User | null>({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      if (!tokenStore.getAccessToken()) return null;
      const { data } = await apiClient.get<ApiResponse<User>>('/auth/me');
      return data.data;
    },
    retry: false,
    staleTime: 5 * 60 * 1000,
  });

  const loginMutation = useMutation({
    mutationFn: async ({ email, password }: { email: string; password: string }) => {
      const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });
      return data.data;
    },
    onSuccess: (data) => {
      tokenStore.setAccessToken(data.accessToken);
      queryClient.setQueryData(['auth', 'me'], data.user);
    },
  });

  const signupMutation = useMutation({
    mutationFn: async ({ name, email, password }: { name: string; email: string; password: string }) => {
      const { data } = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', { name, email, password });
      return data.data;
    },
    onSuccess: (data) => {
      tokenStore.setAccessToken(data.accessToken);
      queryClient.setQueryData(['auth', 'me'], data.user);
    },
  });

  const logoutMutation = useMutation({
    mutationFn: async () => {
      await apiClient.post('/auth/logout', {});
    },
    onSettled: () => {
      tokenStore.clearAccessToken();
      queryClient.clear();
    },
  });

  return {
    user: user ?? null,
    isAuthenticated: !!user,
    isLoading,
    login: loginMutation.mutateAsync,
    signup: signupMutation.mutateAsync,
    logout: logoutMutation.mutateAsync,
    loginError: loginMutation.error,
    signupError: signupMutation.error,
  };
}
```

- [ ] **Step 2: Simplificar `src/store/finance.ts` — remover dados, manter apenas UI state**

Substitua todo o conteúdo do store pelo estado de UI apenas:

```typescript
import { create } from 'zustand';

interface FinanceUIState {
  selectedAccountId: string | null;
  selectedCategoryId: string | null;
  transactionModalOpen: boolean;
  setSelectedAccount: (id: string | null) => void;
  setSelectedCategory: (id: string | null) => void;
  setTransactionModalOpen: (open: boolean) => void;
}

export const useFinanceUI = create<FinanceUIState>((set) => ({
  selectedAccountId: null,
  selectedCategoryId: null,
  transactionModalOpen: false,
  setSelectedAccount: (id) => set({ selectedAccountId: id }),
  setSelectedCategory: (id) => set({ selectedCategoryId: id }),
  setTransactionModalOpen: (open) => set({ transactionModalOpen: open }),
}));

export const formatBRL = (v: number) =>
  v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
```

- [ ] **Step 3: Atualizar `src/routes/login.tsx` para usar `useAuth`**

Localize o handler de submit no componente de login e substitua a chamada ao Zustand:

```typescript
// Antes:
const { login } = useFinance();
await login(email, password);

// Depois:
const { login } = useAuth();
await login({ email, password });
```

Adicione tratamento de erro exibindo `loginError?.message` no formulário.

- [ ] **Step 4: Atualizar `src/routes/signup.tsx` para usar `useAuth`**

```typescript
// Antes:
const { signup } = useFinance();
await signup(name, email, password);

// Depois:
const { signup } = useAuth();
await signup({ name, email, password });
```

- [ ] **Step 5: Atualizar `src/routes/forgot-password.tsx` para chamar a API**

```typescript
import { apiClient } from '@/lib/api-client';

// No handler de submit:
await apiClient.post('/auth/forgot-password', { email });
// Exibir mensagem de sucesso genérica independente do resultado
```

- [ ] **Step 6: Atualizar a rota protegida em `src/routes/app.tsx`**

Substitua a verificação de `authed` do Zustand pela verificação do `useAuth`:

```typescript
import { useAuth } from '@/hooks/use-auth';
import { Navigate } from '@tanstack/react-router';

// No componente:
const { isAuthenticated, isLoading } = useAuth();
if (isLoading) return <LoadingSpinner />;
if (!isAuthenticated) return <Navigate to="/login" />;
```

- [ ] **Step 7: Verificar compilação TypeScript**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
npm run build 2>&1 | tail -30
```

Esperado: erros apenas nos componentes que ainda usam `paid` ou dados do antigo Zustand (serão corrigidos na próxima task).

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: migrate auth to api with react query and remove zustand data store"
```

---

### Task 18: Migrar dados (Accounts, Categories, Transactions) para React Query

**Files:**
- Create: `src/hooks/use-accounts.ts`
- Create: `src/hooks/use-categories.ts`
- Create: `src/hooks/use-transactions.ts`
- Create: `src/hooks/use-dashboard.ts`
- Modify: `src/routes/app.accounts.tsx`
- Modify: `src/routes/app.categories.tsx`
- Modify: `src/routes/app.transactions.tsx`
- Modify: `src/routes/app.index.tsx`
- Modify: `src/components/transaction-form.tsx`

**Interfaces:**
- Consumes: `apiClient` (Task 16), tipos `Account`, `Category`, `Transaction`, `PageResponse`, `ApiResponse` (Task 16)
- Produces: hooks React Query para todos os recursos de dados

- [ ] **Step 1: Criar `src/hooks/use-accounts.ts`**

```typescript
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import type { Account, ApiResponse } from '@/types/finance';

const ACCOUNTS_KEY = ['accounts'];

export function useAccounts() {
  return useQuery({
    queryKey: ACCOUNTS_KEY,
    queryFn: async () => {
      const { data } = await apiClient.get<ApiResponse<Account[]>>('/accounts');
      return data.data;
    },
  });
}

export function useCreateAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { name: string; type: string; initialBalance: number; color: string }) => {
      const { data } = await apiClient.post<ApiResponse<Account>>('/accounts', payload);
      return data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}

export function useUpdateAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, ...payload }: { id: string; name: string; type: string; color: string }) => {
      const { data } = await apiClient.put<ApiResponse<Account>>(`/accounts/${id}`, payload);
      return data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(`/accounts/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ACCOUNTS_KEY }),
  });
}
```

- [ ] **Step 2: Criar `src/hooks/use-categories.ts`**

```typescript
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import type { Category, ApiResponse } from '@/types/finance';

const CATEGORIES_KEY = ['categories'];

export function useCategories(type?: 'income' | 'expense') {
  return useQuery({
    queryKey: [...CATEGORIES_KEY, type],
    queryFn: async () => {
      const params = type ? { type } : {};
      const { data } = await apiClient.get<ApiResponse<Category[]>>('/categories', { params });
      return data.data;
    },
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: { name: string; type: string; icon?: string; color: string }) => {
      const { data } = await apiClient.post<ApiResponse<Category>>('/categories', payload);
      return data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY }),
  });
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, ...payload }: { id: string; name: string; icon?: string; color: string }) => {
      const { data } = await apiClient.put<ApiResponse<Category>>(`/categories/${id}`, payload);
      return data.data;
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY }),
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(`/categories/${id}`);
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY }),
  });
}
```

- [ ] **Step 3: Criar `src/hooks/use-transactions.ts`**

```typescript
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import type { Transaction, ApiResponse, PageResponse, TransactionStatus } from '@/types/finance';

const TRANSACTIONS_KEY = ['transactions'];

export interface TransactionFilters {
  startDate?: string;
  endDate?: string;
  categoryId?: string;
  accountId?: string;
  type?: 'income' | 'expense';
  status?: TransactionStatus;
  page?: number;
  size?: number;
  sort?: string;
}

export function useTransactions(filters: TransactionFilters = {}) {
  return useQuery({
    queryKey: [...TRANSACTIONS_KEY, filters],
    queryFn: async () => {
      const { data } = await apiClient.get<ApiResponse<PageResponse<Transaction>>>('/transactions', {
        params: { ...filters, page: filters.page ?? 0, size: filters.size ?? 20 },
      });
      return data.data;
    },
  });
}

export function useCreateTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload: {
      accountId: string;
      categoryId?: string;
      type: string;
      amount: number;
      description?: string;
      transactionDate: string;
      status: TransactionStatus;
    }) => {
      const { data } = await apiClient.post<ApiResponse<Transaction>>('/transactions', payload);
      return data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TRANSACTIONS_KEY });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useUpdateTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, ...payload }: { id: string; accountId: string; categoryId?: string; type: string; amount: number; description?: string; transactionDate: string; status: TransactionStatus }) => {
      const { data } = await apiClient.put<ApiResponse<Transaction>>(`/transactions/${id}`, payload);
      return data.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TRANSACTIONS_KEY });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useDeleteTransaction() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(`/transactions/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TRANSACTIONS_KEY });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
```

- [ ] **Step 4: Criar `src/hooks/use-dashboard.ts`**

```typescript
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import type { ApiResponse } from '@/types/finance';

interface DashboardSummary {
  totalBalance: number;
  monthIncome: number;
  monthExpense: number;
  year: number;
  month: number;
}

interface MonthlySeries {
  label: string;
  year: number;
  month: number;
  income: number;
  expense: number;
}

interface CategoryBreakdown {
  categoryId: string;
  categoryName: string;
  color: string;
  total: number;
}

export function useDashboardSummary(year?: number, month?: number) {
  return useQuery({
    queryKey: ['dashboard', 'summary', year, month],
    queryFn: async () => {
      const { data } = await apiClient.get<ApiResponse<DashboardSummary>>('/dashboard/summary', {
        params: year ? { year, month } : {},
      });
      return data.data;
    },
  });
}

export function useDashboardMonthlySeries(months = 6) {
  return useQuery({
    queryKey: ['dashboard', 'monthly-series', months],
    queryFn: async () => {
      const { data } = await apiClient.get<ApiResponse<MonthlySeries[]>>('/dashboard/monthly-series', {
        params: { months },
      });
      return data.data;
    },
  });
}

export function useDashboardByCategory(year?: number, month?: number, type = 'expense') {
  return useQuery({
    queryKey: ['dashboard', 'by-category', year, month, type],
    queryFn: async () => {
      const { data } = await apiClient.get<ApiResponse<CategoryBreakdown[]>>('/dashboard/by-category', {
        params: { type, ...(year ? { year, month } : {}) },
      });
      return data.data;
    },
  });
}

export function useDashboardRecentTransactions(limit = 6) {
  return useQuery({
    queryKey: ['dashboard', 'recent', limit],
    queryFn: async () => {
      const { data } = await apiClient.get('/dashboard/recent-transactions', { params: { limit } });
      return data.data;
    },
  });
}
```

- [ ] **Step 5: Atualizar `src/routes/app.index.tsx` (Dashboard)**

Substitua as importações de `useFinance` e `finance-utils` pelos novos hooks:

```typescript
// Remover:
import { useFinance, formatBRL } from '@/store/finance';
import { accountBalance, byCategory, monthTotals, monthlySeries, totalBalance } from '@/lib/finance-utils';

// Adicionar:
import { useDashboardSummary, useDashboardMonthlySeries, useDashboardByCategory, useDashboardRecentTransactions } from '@/hooks/use-dashboard';
import { useAccounts } from '@/hooks/use-accounts';
import { formatBRL } from '@/store/finance';
```

No corpo do componente `Dashboard`:
```typescript
const { data: summary } = useDashboardSummary();
const { data: series = [] } = useDashboardMonthlySeries(6);
const { data: catData = [] } = useDashboardByCategory();
const { data: recent = [] } = useDashboardRecentTransactions(6);
const { data: accounts = [] } = useAccounts();

// Substituir:
// const totals = monthTotals(...)   →  summary?.monthIncome, summary?.monthExpense
// const total = totalBalance(...)   →  summary?.totalBalance
// const catData = byCategory(...)   →  catData (do hook)
// const series = monthlySeries(...) →  series (do hook)
// const recent = transactions.slice →  recent (do hook)
```

Atualize as referências nos JSX para usar os dados dos hooks. O campo `paid` não existe mais — substitua por `status`:
```typescript
// Antes: t.paid ? 'Pago' : 'Pendente'
// Depois: t.status === 'PAID' ? 'Pago' : t.status
```

- [ ] **Step 6: Atualizar `src/routes/app.accounts.tsx`**

```typescript
// Remover importação de useFinance
// Adicionar:
import { useAccounts, useCreateAccount, useUpdateAccount, useDeleteAccount } from '@/hooks/use-accounts';

// No componente:
const { data: accounts = [], isLoading } = useAccounts();
const createAccount = useCreateAccount();
const updateAccount = useUpdateAccount();
const deleteAccount = useDeleteAccount();

// Substituir chamadas ao Zustand:
// addAccount(data)    → createAccount.mutate(data)
// updateAccount(id, data) → updateAccount.mutate({ id, ...data })
// deleteAccount(id)   → deleteAccount.mutate(id)
```

Remover campo `initialBalance` do display (agora é `balance`).

- [ ] **Step 7: Atualizar `src/routes/app.categories.tsx`**

```typescript
import { useCategories, useCreateCategory, useUpdateCategory, useDeleteCategory } from '@/hooks/use-categories';

const { data: categories = [] } = useCategories();
const createCategory = useCreateCategory();
// ... mesmo padrão
```

- [ ] **Step 8: Atualizar `src/routes/app.transactions.tsx`**

```typescript
import { useTransactions, useCreateTransaction, useUpdateTransaction, useDeleteTransaction } from '@/hooks/use-transactions';
import { useAccounts } from '@/hooks/use-accounts';
import { useCategories } from '@/hooks/use-categories';

const { data: txPage } = useTransactions(filters);
const transactions = txPage?.content ?? [];
// ...
```

Atualizar filtros para usar `status` em vez de `paid`. Adicionar paginação baseada em `txPage.totalPages`.

- [ ] **Step 9: Atualizar `src/components/transaction-form.tsx`**

```typescript
import { useCreateTransaction, useUpdateTransaction } from '@/hooks/use-transactions';
import { useAccounts } from '@/hooks/use-accounts';
import { useCategories } from '@/hooks/use-categories';
import type { TransactionStatus } from '@/types/finance';

// Substituir:
// addTransaction / updateTransaction do Zustand → createTransaction.mutate / updateTransaction.mutate

// O campo 'paid: boolean' se torna 'status: TransactionStatus'
// No formulário, adicionar select com: PENDING, PAID, OVERDUE, CANCELLED
```

- [ ] **Step 10: Verificar build sem erros de tipo**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
npm run build 2>&1 | grep -E "error|Error" | head -20
```

Esperado: zero erros TypeScript.

- [ ] **Step 11: Commit**

```bash
git add src/
git commit -m "feat: migrate all data hooks to react query consuming backend api"
```

---

### Task 19: Remover mocks, testar integração ponta a ponta

**Files:**
- Delete: `src/mocks/seed.ts`
- Modify: `src/lib/finance-utils.ts` (remover funções que calculavam dados localmente)
- Modify: `src/routes/app.settings.tsx` (atualizar perfil via API)

**Interfaces:**
- Produces: frontend 100% integrado ao backend, sem dados mockados

- [ ] **Step 1: Remover `src/mocks/seed.ts`**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
rm src/mocks/seed.ts
```

- [ ] **Step 2: Limpar `src/lib/finance-utils.ts`**

Remova as funções `accountBalance`, `byCategory`, `monthTotals`, `monthlySeries`, `totalBalance` — agora fornecidas pela API via `use-dashboard.ts`.

Mantenha apenas utilitários que não dependem de dados locais (formatação de datas, etc.), ou delete o arquivo inteiro se estiver vazio.

- [ ] **Step 3: Atualizar `src/routes/app.settings.tsx` para usar a API de perfil**

```typescript
import { useAuth } from '@/hooks/use-auth';

// No componente:
const { user } = useAuth();
// Exibir user.name, user.email, user.emailVerified
```

- [ ] **Step 4: Verificar build final**

```bash
npm run build
```

Esperado: `BUILD SUCCESSFUL` sem erros.

- [ ] **Step 5: Testar integração ponta a ponta**

Com o backend rodando localmente (`./gradlew bootRun --args='--spring.profiles.active=dev'`):

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
npm run dev &
sleep 5
```

Abra `http://localhost:5173` no browser e verifique:
- [ ] Página de login abre corretamente
- [ ] Cadastro de novo usuário funciona (retorna token)
- [ ] Dashboard carrega com dados da API (zerado para novo usuário)
- [ ] Criação de conta bancária funciona e aparece na lista
- [ ] Criação de categoria funciona
- [ ] Criação de transação funciona e atualiza o saldo da conta
- [ ] Dashboard exibe totais atualizados após criar transações
- [ ] Logout funciona e redireciona para login

- [ ] **Step 6: Commit final**

```bash
cd /home/erezende/financa-facil/financa-facil-frontend
git add -A
git commit -m "feat: remove mocks, complete frontend integration with backend api"
```
