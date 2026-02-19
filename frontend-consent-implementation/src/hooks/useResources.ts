/**
 * useResources Hook
 * 
 * A custom React hook for fetching and managing resources.
 * Provides a clean interface for components to interact with the resource service.
 */
import { useState, useEffect, useCallback } from 'react';
import resourceService, { Resource } from '../services/resourceService';

interface UseResourcesOptions {
  autoFetch?: boolean;
  initialType?: string;
  initialCategory?: string;
  featuredOnly?: boolean;
}

interface UseResourcesReturn {
  resources: Resource[];
  featuredResources: Resource[];
  categories: string[];
  loading: boolean;
  error: string | null;
  searchResources: (query: string) => Promise<void>;
  filterByType: (type: string) => Promise<void>;
  filterByCategory: (category: string) => Promise<void>;
  getFeatured: () => Promise<void>;
  getAllResources: () => Promise<void>;
  downloadResource: (id: number) => Promise<{ blob: Blob; fileName: string }>;
  refresh: () => Promise<void>;
}

/**
 * Custom hook for resource management
 */
export const useResources = (options: UseResourcesOptions = {}): UseResourcesReturn => {
  const {
    autoFetch = true,
    initialType = '',
    initialCategory = '',
    featuredOnly = false
  } = options;

  const [resources, setResources] = useState<Resource[]>([]);
  const [featuredResources, setFeaturedResources] = useState<Resource[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch all resources
  const getAllResources = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await resourceService.getAllResources();
      setResources(data);
    } catch (err) {
      console.error('Failed to fetch resources:', err);
      setError('Failed to load resources. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Search resources
  const searchResources = useCallback(async (query: string) => {
    if (!query.trim()) {
      await getAllResources();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await resourceService.searchResources(query);
      setResources(data);
    } catch (err) {
      console.error('Search failed:', err);
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [getAllResources]);

  // Filter by type
  const filterByType = useCallback(async (type: string) => {
    if (!type) {
      await getAllResources();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await resourceService.getResourcesByType(type);
      setResources(data);
    } catch (err) {
      console.error('Filter by type failed:', err);
      setError('Failed to filter resources.');
    } finally {
      setLoading(false);
    }
  }, [getAllResources]);

  // Filter by category
  const filterByCategory = useCallback(async (category: string) => {
    if (!category) {
      await getAllResources();
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await resourceService.getResourcesByCategory(category);
      setResources(data);
    } catch (err) {
      console.error('Filter by category failed:', err);
      setError('Failed to filter resources.');
    } finally {
      setLoading(false);
    }
  }, [getAllResources]);

  // Get featured resources
  const getFeatured = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await resourceService.getFeaturedResources();
      setResources(data);
    } catch (err) {
      console.error('Failed to fetch featured resources:', err);
      setError('Failed to load featured resources.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Download resource
  const downloadResource = useCallback(async (id: number) => {
    const result = await resourceService.downloadResource(id);
    return result;
  }, []);

  // Refresh all data
  const refresh = useCallback(async () => {
    await getAllResources();
  }, [getAllResources]);

  // Initial fetch
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Fetch categories and featured resources in parallel
        const [cats, featured] = await Promise.all([
          resourceService.getResourceCategories(),
          resourceService.getFeaturedResources()
        ]);
        
        setCategories(cats);
        setFeaturedResources(featured);

        // Fetch main resources based on options
        if (featuredOnly) {
          const data = await resourceService.getFeaturedResources();
          setResources(data);
        } else if (initialType) {
          const data = await resourceService.getResourcesByType(initialType);
          setResources(data);
        } else if (initialCategory) {
          const data = await resourceService.getResourcesByCategory(initialCategory);
          setResources(data);
        } else {
          const data = await resourceService.getAllResources();
          setResources(data);
        }
        
        setError(null);
      } catch (err) {
        console.error('Failed to fetch initial data:', err);
        setError('Failed to load resources. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    if (autoFetch) {
      fetchData();
    }
  }, [autoFetch, featuredOnly, initialType, initialCategory]);

  return {
    resources,
    featuredResources,
    categories,
    loading,
    error,
    searchResources,
    filterByType,
    filterByCategory,
    getFeatured,
    getAllResources,
    downloadResource,
    refresh
  };
};

export default useResources;