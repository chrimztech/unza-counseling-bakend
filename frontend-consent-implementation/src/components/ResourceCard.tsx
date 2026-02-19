/**
 * ResourceCard Component
 * 
 * A reusable card component for displaying a single resource.
 */
import React from 'react';
import { Resource } from '../services/resourceService';
import resourceService from '../services/resourceService';

interface ResourceCardProps {
  resource: Resource;
  onDownload?: (resource: Resource) => void;
  compact?: boolean;
}

const ResourceCard: React.FC<ResourceCardProps> = ({ resource, onDownload, compact = false }) => {
  // Get icon based on resource type
  const getIcon = () => {
    const iconType = resourceService.getFileIcon(resource.type, resource.fileType);
    
    const iconClass = compact ? "w-6 h-6" : "w-8 h-8";
    
    switch (iconType) {
      case 'pdf':
        return (
          <svg className={iconClass} fill="currentColor" viewBox="0 0 24 24">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6zm-1 2l5 5h-5V4zM8.5 13c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5zm3 0c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5zm3 0c.28 0 .5.22.5.5v3c0 .28-.22.5-.5.5s-.5-.22-.5-.5v-3c0-.28.22-.5.5-.5z"/>
          </svg>
        );
      case 'video':
        return (
          <svg className={iconClass} fill="currentColor" viewBox="0 0 24 24">
            <path d="M8 5v14l11-7z"/>
          </svg>
        );
      case 'audio':
        return (
          <svg className={iconClass} fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
          </svg>
        );
      case 'image':
        return (
          <svg className={iconClass} fill="currentColor" viewBox="0 0 24 24">
            <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
          </svg>
        );
      default:
        return (
          <svg className={iconClass} fill="currentColor" viewBox="0 0 24 24">
            <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
          </svg>
        );
    }
  };

  // Get color class based on type
  const getColorClass = () => {
    const colors: Record<string, string> = {
      ARTICLE: 'bg-blue-100 text-blue-800 border-blue-200',
      VIDEO: 'bg-red-100 text-red-800 border-red-200',
      DOCUMENT: 'bg-green-100 text-green-800 border-green-200',
      AUDIO: 'bg-purple-100 text-purple-800 border-purple-200',
      IMAGE: 'bg-yellow-100 text-yellow-800 border-yellow-200'
    };
    return colors[resource.type] || 'bg-gray-100 text-gray-800 border-gray-200';
  };

  const handleDownload = () => {
    if (onDownload) {
      onDownload(resource);
    }
  };

  if (compact) {
    return (
      <div className="flex items-center gap-3 p-3 bg-white rounded-lg border border-gray-200 hover:shadow-md transition-shadow">
        <div className={`p-2 rounded ${getColorClass()}`}>
          {getIcon()}
        </div>
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-medium text-gray-900 truncate">
            {resource.title}
          </h4>
          <p className="text-xs text-gray-500">
            {resource.type} {resource.fileSize && `· ${resourceService.formatFileSize(resource.fileSize)}`}
          </p>
        </div>
        {resource.fileKey && (
          <button
            onClick={handleDownload}
            className="px-3 py-1 text-sm text-blue-600 hover:text-blue-800"
          >
            Download
          </button>
        )}
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
      {/* Resource Icon/Header */}
      <div className={`p-6 ${getColorClass()}`}>
        <div className="flex items-center justify-between">
          <div className="flex items-center justify-center w-16 h-16 bg-white bg-opacity-50 rounded-lg">
            {getIcon()}
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
              onClick={handleDownload}
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
  );
};

export default ResourceCard;