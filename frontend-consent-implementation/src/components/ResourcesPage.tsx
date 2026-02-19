/**
 * ResourcesPage Component
 * 
 * Main page for clients to view and access mental health resources.
 * This component displays resources in a grid/list format with filtering options.
 * 
 * No authentication required - resources are publicly accessible.
 */
import React, { useState, useEffect, useCallback } from 'react';
import resourceService, { Resource } from '../services/resourceService';

// Icons (using simple SVG icons or replace with your icon library)
const FileIcons: Record<string, JSX.Element> = {
  pdf: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm-1 2l5 5h-5V4zM8.5 13c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5zm3 0c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5zm3 0c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5z"/>
    </svg>
  ),
  video: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M8 5v14l11-7z"/>
    </svg>
  ),
  audio: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
    </svg>
  ),
  image: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
    </svg>
  ),
  document: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
    </svg>
  ),
  article: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
    </svg>
  ),
  file: (
    <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 24 24">
      <path d="M6 2c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13z"/>
    </svg>
  )
};

// Color mapping for resource types
const TypeColors: Record<string, string> = {
  ARTICLE: 'bg-blue-100 text-blue-800 border-blue-200',
  VIDEO: 'bg-red-100 text-red-800 border-red-200',
  DOCUMENT: 'bg-green-100 text-green-800 border-green-200',
  AUDIO: 'bg-purple-100 text-purple-800 border-purple-200',
  IMAGE: 'bg-yellow-100 text-yellow-800 border-yellow-200'
};

const ResourcesPage: React.FC = () => {
  const [resources, setResources] = useState<Resource[]>([]);
  const [featuredResources, setFeaturedResources] = useState<Resource[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Filter states
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [showFeaturedOnly, setShowFeaturedOnly] = useState(false);

  // Fetch initial data
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        setLoading(true);
        const [allResources, featured, cats] = await Promise.all([
          resourceService.getAllResources(),
          resourceService.getFeaturedResources(),
          resourceService.getResourceCategories()
        ]);
        setResources(allResources);
        setFeaturedResources(featured);
        setCategories(cats);
        setError(null);
      } catch (err) {
        console.error('Failed to fetch resources:', err);
        setError('Failed to load resources. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchInitialData();
  }, []);

  // Handle search
  const handleSearch = useCallback(async () => {
    if (!searchQuery.trim()) {
      const allResources = await resourceService.getAllResources();
      setResources(allResources);
      return;
    }

    try {
      setLoading(true);
      const results = await resourceService.searchResources(searchQuery);
      setResources(results);
      setError(null);
    } catch (err) {
      console.error('Search failed:', err);
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [searchQuery]);

  // Handle type filter
  const handleTypeFilter = useCallback(async (type: string) => {
    setSelectedType(type);
    setSelectedCategory('');
    setShowFeaturedOnly(false);
    
    if (!type) {
      const allResources = await resourceService.getAllResources();
      setResources(allResources);
      return;
    }

    try {
      setLoading(true);
      const results = await resourceService.getResourcesByType(type);
      setResources(results);
      setError(null);
    } catch (err) {
      console.error('Filter by type failed:', err);
      setError('Failed to filter resources.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Handle category filter
  const handleCategoryFilter = useCallback(async (category: string) => {
    setSelectedCategory(category);
    setSelectedType('');
    setShowFeaturedOnly(false);
    
    if (!category) {
      const allResources = await resourceService.getAllResources();
      setResources(allResources);
      return;
    }

    try {
      setLoading(true);
      const results = await resourceService.getResourcesByCategory(category);
      setResources(results);
      setError(null);
    } catch (err) {
      console.error('Filter by category failed:', err);
      setError('Failed to filter resources.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Handle featured filter
  const handleFeaturedFilter = useCallback(() => {
    setShowFeaturedOnly(!showFeaturedOnly);
    setSelectedType('');
    setSelectedCategory('');
    
    if (!showFeaturedOnly) {
      setResources(featuredResources);
    } else {
      resourceService.getAllResources().then(setResources);
    }
  }, [showFeaturedOnly, featuredResources]);

  // Handle download
  const handleDownload = async (resource: Resource) => {
    try {
      const { blob, fileName } = await resourceService.downloadResource(resource.id);
      resourceService.triggerDownload(blob, fileName);
    } catch (err) {
      console.error('Download failed:', err);
      alert('Failed to download resource. Please try again.');
    }
  };

  // Get icon for resource
  const getResourceIcon = (resource: Resource): JSX.Element => {
    const iconType = resourceService.getFileIcon(resource.type, resource.fileType);
    return FileIcons[iconType] || FileIcons.file;
  };

  // Clear all filters
  const clearFilters = async () => {
    setSearchQuery('');
    setSelectedType('');
    setSelectedCategory('');
    setShowFeaturedOnly(false);
    
    try {
      setLoading(true);
      const allResources = await resourceService.getAllResources();
      setResources(allResources);
      setError(null);
    } catch (err) {
      console.error('Failed to clear filters:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && resources.length === 0) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header Section */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-3xl font-bold">Mental Health Resources</h1>
          <p className="mt-2 text-blue-100">
            Explore our collection of articles, videos, and documents to support your mental health journey.
          </p>
        </div>
      </div>

      {/* Featured Resources Section */}
      {featuredResources.length > 0 && !showFeaturedOnly && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">Featured Resources</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {featuredResources.slice(0, 3).map((resource) => (
              <div
                key={resource.id}
                className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow cursor-pointer"
                onClick={() => handleDownload(resource)}
              >
                <div className={`p-4 ${TypeColors[resource.type] || 'bg-gray-100'}`}>
                  <div className="flex items-center justify-center h-16">
                    {getResourceIcon(resource)}
                  </div>
                </div>
                <div className="p-4">
                  <span className="text-xs font-medium text-blue-600 uppercase">
                    {resource.type}
                  </span>
                  <h3 className="mt-1 font-semibold text-gray-900 line-clamp-2">
                    {resource.title}
                  </h3>
                  <p className="mt-2 text-sm text-gray-600 line-clamp-2">
                    {resource.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Search and Filter Section */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex flex-col md:flex-row gap-4">
            {/* Search Input */}
            <div className="flex-1">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search resources..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <svg
                  className="absolute left-3 top-2.5 h-5 w-5 text-gray-400"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
                <button
                  onClick={handleSearch}
                  className="absolute right-2 top-1.5 px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
                >
                  Search
                </button>
              </div>
            </div>

            {/* Type Filter */}
            <div className="flex gap-2">
              <select
                value={selectedType}
                onChange={(e) => handleTypeFilter(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Types</option>
                <option value="ARTICLE">Articles</option>
                <option value="VIDEO">Videos</option>
                <option value="DOCUMENT">Documents</option>
                <option value="AUDIO">Audio</option>
                <option value="IMAGE">Images</option>
              </select>

              {/* Category Filter */}
              <select
                value={selectedCategory}
                onChange={(e) => handleCategoryFilter(e.target.value)}
                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Categories</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>

              {/* Featured Toggle */}
              <button
                onClick={handleFeaturedFilter}
                className={`px-4 py-2 rounded-lg border ${
                  showFeaturedOnly
                    ? 'bg-yellow-100 border-yellow-500 text-yellow-800'
                    : 'border-gray-300 text-gray-700 hover:bg-gray-50'
                }`}
              >
                Featured
              </button>

              {/* Clear Filters */}
              {(searchQuery || selectedType || selectedCategory || showFeaturedOnly) && (
                <button
                  onClick={clearFilters}
                  className="px-4 py-2 text-red-600 hover:text-red-800"
                >
                  Clear
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        </div>
      )}

      {/* Resources Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-12">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-gray-900">
            {showFeaturedOnly
              ? 'Featured Resources'
              : selectedType
              ? `${selectedType}s`
              : selectedCategory
              ? selectedCategory
              : 'All Resources'}
            <span className="ml-2 text-sm font-normal text-gray-500">
              ({resources.length} found)
            </span>
          </h2>
        </div>

        {resources.length === 0 ? (
          <div className="text-center py-12">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">No resources found</h3>
            <p className="mt-1 text-sm text-gray-500">
              Try adjusting your search or filter criteria.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {resources.map((resource) => (
              <div
                key={resource.id}
                className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
              >
                {/* Resource Icon/Header */}
                <div className={`p-6 ${TypeColors[resource.type] || 'bg-gray-100'}`}>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center justify-center w-16 h-16 bg-white bg-opacity-50 rounded-lg">
                      {getResourceIcon(resource)}
                    </div>
                    {resource.featured && (
                      <span className="px-2 py-1 bg-yellow-400 text-yellow-900 text-xs font-medium rounded">
                        Featured
                      </span>
                    )}
                  </div>
                </div>

                {/* Resource Content */}
                <div className="p-4">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-xs font-medium text-blue-600 uppercase">
                      {resource.type}
                    </span>
                    {resource.category && (
                      <span className="text-xs text-gray-500">
                        in {resource.category}
                      </span>
                    )}
                  </div>
                  
                  <h3 className="font-semibold text-gray-900 line-clamp-2 mb-2">
                    {resource.title}
                  </h3>
                  
                  <p className="text-sm text-gray-600 line-clamp-3 mb-3">
                    {resource.description}
                  </p>

                  {/* File Info */}
                  {resource.fileName && (
                    <div className="text-xs text-gray-500 mb-3">
                      <span className="font-medium">{resource.fileName}</span>
                      {resource.fileSize && (
                        <span className="ml-2">
                          ({resourceService.formatFileSize(resource.fileSize)})
                        </span>
                      )}
                    </div>
                  )}

                  {/* Date */}
                  <div className="text-xs text-gray-400 mb-3">
                    Added {resourceService.formatDate(resource.createdAt)}
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-2">
                    {resource.fileKey ? (
                      <button
                        onClick={() => handleDownload(resource)}
                        className="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        Download
                      </button>
                    ) : resource.url ? (
                      <a
                        href={resource.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex-1 px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors text-center"
                      >
                        View Resource
                      </a>
                    ) : (
                      <button
                        disabled
                        className="flex-1 px-4 py-2 bg-gray-300 text-gray-500 text-sm font-medium rounded-lg cursor-not-allowed"
                      >
                        Unavailable
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ResourcesPage;