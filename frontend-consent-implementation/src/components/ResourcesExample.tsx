/**
 * ResourcesExample Component
 * 
 * A simplified example showing how to use the useResources hook
 * and ResourceCard component in your application.
 */
import React from 'react';
import { useResources } from '../hooks/useResources';
import ResourceCard from './ResourceCard';
import resourceService from '../services/resourceService';

const ResourcesExample: React.FC = () => {
  const {
    resources,
    featuredResources,
    categories,
    loading,
    error,
    searchResources,
    filterByType,
    filterByCategory,
    downloadResource
  } = useResources();

  const [searchQuery, setSearchQuery] = React.useState('');

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    searchResources(searchQuery);
  };

  const handleDownload = async (resource: { id: number }) => {
    try {
      const { blob, fileName } = await downloadResource(resource.id);
      resourceService.triggerDownload(blob, fileName);
    } catch (err) {
      console.error('Download failed:', err);
      alert('Failed to download resource');
    }
  };

  if (loading) {
    return <div className="p-4">Loading resources...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">{error}</div>;
  }

  return (
    <div className="p-4">
      <h1 className="text-2xl font-bold mb-4">Mental Health Resources</h1>

      {/* Search Form */}
      <form onSubmit={handleSearch} className="mb-4">
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search resources..."
          className="border p-2 rounded mr-2"
        />
        <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">
          Search
        </button>
      </form>

      {/* Type Filters */}
      <div className="mb-4 flex gap-2">
        <button onClick={() => filterByType('')} className="px-3 py-1 border rounded">
          All
        </button>
        <button onClick={() => filterByType('ARTICLE')} className="px-3 py-1 border rounded">
          Articles
        </button>
        <button onClick={() => filterByType('VIDEO')} className="px-3 py-1 border rounded">
          Videos
        </button>
        <button onClick={() => filterByType('DOCUMENT')} className="px-3 py-1 border rounded">
          Documents
        </button>
      </div>

      {/* Category Filters */}
      <div className="mb-4 flex gap-2 flex-wrap">
        {categories.map((category) => (
          <button
            key={category}
            onClick={() => filterByCategory(category)}
            className="px-3 py-1 border rounded text-sm"
          >
            {category}
          </button>
        ))}
      </div>

      {/* Featured Resources */}
      {featuredResources.length > 0 && (
        <div className="mb-8">
          <h2 className="text-xl font-semibold mb-2">Featured</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {featuredResources.slice(0, 3).map((resource) => (
              <ResourceCard
                key={resource.id}
                resource={resource}
                onDownload={handleDownload}
              />
            ))}
          </div>
        </div>
      )}

      {/* All Resources */}
      <h2 className="text-xl font-semibold mb-2">All Resources ({resources.length})</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {resources.map((resource) => (
          <ResourceCard
            key={resource.id}
            resource={resource}
            onDownload={handleDownload}
          />
        ))}
      </div>
    </div>
  );
};

export default ResourcesExample;