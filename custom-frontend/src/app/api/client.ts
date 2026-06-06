const API_BASE_URL = '/api';

export type RequestOptions = RequestInit & { skipAuth?: boolean };

class ApiClient {
  private buildHeaders(skipAuth: boolean, extra?: HeadersInit): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...extra,
    };

    if (!skipAuth) {
      const token = localStorage.getItem('token');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    return headers;
  }

  async request<T>(endpoint: string, options?: RequestOptions): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const skipAuth = options?.skipAuth ?? false;
    const { skipAuth: _skip, ...fetchInit } = options ?? {};

    try {
      const response = await fetch(url, {
        ...fetchInit,
        headers: {
          ...this.buildHeaders(skipAuth, fetchInit.headers as HeadersInit),
        },
      });

      if (response.status === 401) {
        if (!skipAuth) {
          localStorage.removeItem('token');
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
        throw new Error(skipAuth ? 'Не удалось загрузить данные с сервера.' : 'Unauthorized');
      }

      if (!response.ok) {
        const contentTypeErr = response.headers.get('content-type') ?? '';
        let message = `Request failed with status ${response.status}`;
        if (contentTypeErr.includes('application/json')) {
          const error = await response.json().catch(() => null);
          message =
            error &&
            typeof error === 'object' &&
            'message' in error &&
            typeof (error as { message?: string }).message === 'string'
              ? (error as { message: string }).message
              : message;
        } else {
          const text = await response.text().catch(() => '');
          if (text) message = text;
        }
        throw new Error(message);
      }

      const contentType = response.headers.get('content-type') ?? '';
      if (contentType.includes('application/json')) {
        return response.json();
      }

      const textBody = await response.text();
      return (textBody || '') as T;
    } catch (error) {
      throw error;
    }
  }

  async get<T>(endpoint: string, init?: { skipAuth?: boolean }): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET', ...init });
  }

  async post<T>(endpoint: string, data?: unknown, init?: { skipAuth?: boolean }): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data !== undefined ? JSON.stringify(data) : undefined,
      ...init,
    });
  }

  async put<T>(endpoint: string, data?: unknown, init?: { skipAuth?: boolean }): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data !== undefined ? JSON.stringify(data) : undefined,
      ...init,
    });
  }

  async delete<T>(endpoint: string, init?: { skipAuth?: boolean }): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE', ...init });
  }
}

export const apiClient = new ApiClient();
