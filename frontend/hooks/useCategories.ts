/**
 * useCategories hook - Fetch and manage categories data
 */

'use client';

import { useState, useEffect, useCallback } from 'react';
import { adminAPI } from '@/lib/api';
import type { CategoryResponse } from '@/types';

const categoryCache = new Map<string, CategoryResponse[]>();

function categoryCacheKey(parentId?: number | null) {
  return parentId === undefined || parentId === null ? 'all' : String(parentId);
}

interface UseCategoriesOptions {
  parentId?: number | null;
  autoFetch?: boolean;
}

export function useCategories(options: UseCategoriesOptions = {}) {
  const { parentId, autoFetch = true } = options;
  const cached = categoryCache.get(categoryCacheKey(parentId));
  const [categories, setCategories] = useState<CategoryResponse[]>(cached ?? []);
  const [loading, setLoading] = useState(autoFetch && !cached);
  const [error, setError] = useState<string | null>(null);

  const fetchCategories = useCallback(async (fetchParentId?: number | null) => {
    try {
      setLoading(true);
      setError(null);

      const pid = fetchParentId !== undefined ? fetchParentId : parentId;
      const response = await adminAPI.getAllCategories(pid);

      if (response.success && response.data) {
        categoryCache.set(categoryCacheKey(pid), response.data);
        setCategories(response.data);
      } else {
        throw new Error(response.message || 'Lỗi khi tải danh mục');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Lỗi khi tải danh mục';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  }, [parentId]);

  useEffect(() => {
    if (!autoFetch) return;
    const hit = categoryCache.get(categoryCacheKey(parentId));
    if (hit) {
      setCategories(hit);
      setLoading(false);
      return;
    }
    fetchCategories();
  }, [autoFetch, fetchCategories, parentId]);

  return {
    categories,
    loading,
    error,
    refetch: fetchCategories,
  };
}

