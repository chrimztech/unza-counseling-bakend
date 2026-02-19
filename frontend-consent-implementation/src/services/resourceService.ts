/**
 * Resource Service - Frontend integration for mental health resources
 * 
 * This service provides methods to interact with the backend Resource API.
 * All endpoints are public (no authentication required for viewing resources).
 */
import apiClient from './apiClient';

// Resource type definitions matching the backend Resource entity
export interface Resource {
  id: number;
  title: string;
  description: string;
  type: 'ARTICLE' | 'VIDEO' | 'DOCUMENT' | 'AUDIO' | 'IMAGE';
  category: string;
  featured: boolean;
  url?: string;
  createdAt: string;
  // File upload fields
  fileName?: string;
  fileType?: string;
  fileSize?: number;
  fileUrl?: string;
  fileKey?: string;
}

export interface ResourceStatistics {
  totalResources: number;
  byType: Record<string, number>;
  byCategory: Record<string, number>;
  featuredCount: number;
}

/**
 * Resource Service Class
 */
class ResourceService {
  private baseUrl = '/v1/resources';

  /**
   * Get all resources
   * Public endpoint - no authentication required
   */
  async getAllResources(): Promise<Resource[]> {
    try {
      const response = await apiClient.get<Resource[]>(this.baseUrl);
      return response.data;
    } catch (error) {
      console.error('Error fetching all resources:', error);
      throw error;
    }
  }

  /**
   * Search resources by title or description
   * Public endpoint - no authentication required
   */
  async searchResources(query: string): Promise<Resource[]> {
    try {
      const response = await apiClient.get<Resource[]>(`${this.baseUrl}/search`, {
        params: { query }
      });
      return response.data;
    } catch (error) {
      console.error('Error searching resources:', error);
      throw error;
    }
  }

  /**
   * Get all available resource categories
   * Public endpoint - no authentication required
   */
  async getResourceCategories(): Promise<string[]> {
    try {
      const response = await apiClient.get<string[]>(`${this.baseUrl}/categories`);
      return response.data;
    } catch (error) {
      console.error('Error fetching resource categories:', error);
      throw error;
    }
  }

  /**
   * Get resources by type
   * Public endpoint - no authentication required
   * @param type - ARTICLE, VIDEO, DOCUMENT, AUDIO, IMAGE
   */
  async getResourcesByType(type: string): Promise<Resource[]> {
    try {
      const response = await apiClient.get<Resource[]>(`${this.baseUrl}/type/${type}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching resources by type ${type}:`, error);
      throw error;
    }
  }

  /**
   * Get resources by category
   * Public endpoint - no authentication required
   */
  async getResourcesByCategory(category: string): Promise<Resource[]> {
    try {
      const response = await apiClient.get<Resource[]>(`${this.baseUrl}/category/${encodeURIComponent(category)}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching resources by category ${category}:`, error);
      throw error;
    }
  }

  /**
   * Get featured resources
   * Public endpoint - no authentication required
   */
  async getFeaturedResources(): Promise<Resource[]> {
    try {
      const response = await apiClient.get<Resource[]>(`${this.baseUrl}/featured`);
      return response.data;
    } catch (error) {
      console.error('Error fetching featured resources:', error);
      throw error;
    }
  }

  /**
   * Get resource statistics
   * Public endpoint - no authentication required
   */
  async getResourceStatistics(): Promise<ResourceStatistics> {
    try {
      const response = await apiClient.get<ResourceStatistics>(`${this.baseUrl}/stats`);
      return response.data;
    } catch (error) {
      console.error('Error fetching resource statistics:', error);
      throw error;
    }
  }

  /**
   * Download a resource file
   * Public endpoint - no authentication required
   * Returns the file as a blob for download
   */
  async downloadResource(id: number): Promise<{ blob: Blob; fileName: string }> {
    try {
      const response = await apiClient.get(`${this.baseUrl}/download/${id}`, {
        responseType: 'blob'
      });
      
      // Extract filename from Content-Disposition header or use default
      const contentDisposition = response.headers['content-disposition'];
      let fileName = 'download';
      
      if (contentDisposition) {
        const fileNameMatch = contentDisposition.match(/filename=(.+)/);
        if (fileNameMatch && fileNameMatch[1]) {
          fileName = fileNameMatch[1];
        }
      }
      
      return {
        blob: response.data,
        fileName
      };
    } catch (error) {
      console.error(`Error downloading resource ${id}:`, error);
      throw error;
    }
  }

  /**
   * Helper method to trigger browser download
   */
  triggerDownload(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * Get file icon based on file type
   */
  getFileIcon(type: string, fileType?: string): string {
    if (fileType) {
      if (fileType.includes('pdf')) return 'pdf';
      if (fileType.includes('video')) return 'video';
      if (fileType.includes('audio')) return 'audio';
      if (fileType.includes('image')) return 'image';
      if (fileType.includes('word') || fileType.includes('document')) return 'document';
      if (fileType.includes('excel') || fileType.includes('spreadsheet')) return 'spreadsheet';
      if (fileType.includes('powerpoint') || fileType.includes('presentation')) return 'presentation';
    }
    
    // Fallback to resource type
    switch (type) {
      case 'ARTICLE': return 'article';
      case 'VIDEO': return 'video';
      case 'DOCUMENT': return 'document';
      case 'AUDIO': return 'audio';
      case 'IMAGE': return 'image';
      default: return 'file';
    }
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes?: number): string {
    if (!bytes) return '';
    
    const units = ['B', 'KB', 'MB', 'GB'];
    let size = bytes;
    let unitIndex = 0;
    
    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }
    
    return `${size.toFixed(1)} ${units[unitIndex]}`;
  }

  /**
   * Format date for display
   */
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }
}

// Export singleton instance
const resourceService = new ResourceService();
export default resourceService;