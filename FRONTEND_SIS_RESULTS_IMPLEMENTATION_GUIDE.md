# Frontend SIS Results Integration Guide

This guide explains how to integrate the backend's SIS results fetching functionality into your Flutter counseling app.

## Backend API Endpoints

The backend provides the following endpoints for fetching academic results:

### 1. Sync Results from SIS (for a student ID)
```
POST /api/v1/academic-performance/sync/sis
Parameters:
  - studentId (required): The student's ID
  - token (optional): SIS authentication token
  - forceRefresh (optional, default: false): Force refresh from SIS

Response:
{
  "success": true,
  "message": "Results synced successfully from SIS",
  "data": {
    "success": true,
    "message": "...",
    "summary": {
      "totalCourses": 12,
      "passedCourses": 10,
      "failedCourses": 2,
      "averageGpa": 3.2,
      "academicStanding": "GOOD"
    },
    "courses": [...],
    "studentInfo": {...}
  }
}
```

### 2. Sync Results for Client (by internal client ID)
```
POST /api/v1/academic-performance/client/{clientId}/sync/sis
Parameters:
  - token (optional): SIS authentication token
  - forceRefresh (optional, default: false): Force refresh from SIS

Response: Same as above
```

### 3. Get Cached Results (for offline access)
```
GET /api/v1/academic-performance/client/{clientId}/cached/sis

Response: Same as above (but from local database)
```

## Flutter Integration

### Updated ResultsService

Here's the updated Flutter ResultsService that integrates with the backend:

```dart
import 'dart:convert';
import 'dart:io';
import 'dart:async';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../models/results_model.dart';
import 'api_service.dart';

class ResultsService {
  static const int _timeoutDuration = 30;
  static const String _baseUrl = 'http://your-backend-api.com/api/v1/academic-performance';

  /// Fetches student results from the backend (which syncs from SIS)
  static Future<Map<String, dynamic>> fetchStudentResults({
    required String studentId,
    String? token,
    bool forceRefresh = false,
  }) async {
    try {
      // Build request URL
      final url = Uri.parse(
        '$_baseUrl/sync/sis?student_id=$studentId&token=${token ?? ""}&forceRefresh=$forceRefresh'
      );

      // Make API request
      final responseResult = await _makeRequest(url);
      if (responseResult['success'] != true) {
        return responseResult;
      }
      final response = responseResult['response'] as http.Response;

      // Parse response
      return _parseResponse(response);
    } on SocketException catch (e) {
      return _handleError('network', 'No internet connection', e.toString());
    } on TimeoutException catch (e) {
      return _handleError('timeout', 'Request timed out', e.toString());
    } catch (e) {
      return _handleError('unknown', 'Unexpected error', e.toString());
    }
  }

  /// Fetches results for a client by their internal ID
  static Future<Map<String, dynamic>> fetchResultsForClient({
    required int clientId,
    String? token,
    bool forceRefresh = false,
  }) async {
    try {
      final url = Uri.parse(
        '$_baseUrl/client/$clientId/sync/sis?token=${token ?? ""}&forceRefresh=$forceRefresh'
      );

      final responseResult = await _makeRequest(url);
      if (responseResult['success'] != true) {
        return responseResult;
      }
      final response = responseResult['response'] as http.Response;

      return _parseResponse(response);
    } catch (e) {
      return _handleError('unknown', 'Failed to fetch results', e.toString());
    }
  }

  /// Gets cached results (offline support)
  static Future<Map<String, dynamic>> getCachedResults(int clientId) async {
    try {
      final url = Uri.parse('$_baseUrl/client/$clientId/cached/sis');

      final response = await http.get(url).timeout(
        Duration(seconds: _timeoutDuration),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body) as Map<String, dynamic>;
        if (data['success'] == true && data['data'] != null) {
          final resultData = data['data'];
          return {
            'success': true,
            'data': resultData,
            'message': 'Cached results retrieved',
          };
        }
      }

      return {
        'success': false,
        'errorType': 'notFound',
        'message': 'No cached results available',
      };
    } catch (e) {
      return _handleError('unknown', 'Failed to get cached results', e.toString());
    }
  }

  /// Makes HTTP request
  static Future<Map<String, dynamic>> _makeRequest(Uri url) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString('authToken') ?? '';

      final response = await http.post(
        url,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $token',
        },
      ).timeout(
        Duration(seconds: _timeoutDuration),
        onTimeout: () {
          throw TimeoutException('Request timed out');
        },
      );

      if (response.statusCode >= 200 && response.statusCode < 300) {
        return {'success': true, 'response': response};
      } else {
        return _handleHttpError(response.statusCode);
      }
    } on SocketException catch (e) {
      return _handleError('network', 'Connection failed', e.toString());
    } on TimeoutException catch (e) {
      return _handleError('timeout', 'Request timed out', e.toString());
    }
  }

  /// Parses API response
  static Map<String, dynamic> _parseResponse(http.Response response) {
    try {
      final data = jsonDecode(response.body) as Map<String, dynamic>;
      final apiData = data['data'] as Map<String, dynamic>?;

      if (apiData == null || apiData['success'] != true) {
        return {
          'success': false,
          'errorType': 'server',
          'message': apiData?['message'] ?? 'Failed to fetch results',
        };
      }

      // Parse summary
      final summaryData = apiData['summary'] as Map<String, dynamic>?;
      ResultsSummary? summary;
      if (summaryData != null) {
        summary = ResultsSummary(
          totalCourses: summaryData['totalCourses'] as int? ?? 0,
          passedCourses: summaryData['passedCourses'] as int? ?? 0,
          failedCourses: summaryData['failedCourses'] as int? ?? 0,
          averageGpa: (summaryData['averageGpa'] as num?)?.toDouble() ?? 0.0,
          academicStanding: summaryData['academicStanding'] as String? ?? 'UNKNOWN',
        );
      }

      // Parse courses
      final coursesData = apiData['courses'] as List?;
      final courses = coursesData != null
          ? (coursesData as List).map((c) => StudentResult.fromJson(c as Map<String, dynamic>)).toList()
          : <StudentResult>[];

      return {
        'success': true,
        'data': {
          'summary': summary,
          'courses': courses,
          'studentInfo': apiData['studentInfo'],
        },
        'message': 'Results retrieved successfully',
      };
    } catch (e) {
      return _handleError('format', 'Invalid response format', e.toString());
    }
  }

  /// Handles HTTP errors
  static Map<String, dynamic> _handleHttpError(int statusCode) {
    String errorType;
    String message;

    switch (statusCode) {
      case 401:
      case 403:
        errorType = 'auth';
        message = 'Session expired. Please log in again.';
        break;
      case 404:
        errorType = 'notFound';
        message = 'Results not found.';
        break;
      case 500:
      case 503:
        errorType = 'server';
        message = 'Server error. Please try again later.';
        break;
      default:
        errorType = 'http';
        message = 'Request failed with status $statusCode';
    }

    return {'success': false, 'errorType': errorType, 'message': message};
  }

  /// Creates error response
  static Map<String, dynamic> _handleError(String errorType, String message, String details) {
    return {
      'success': false,
      'errorType': errorType,
      'message': message,
      'technicalDetails': details,
    };
  }
}
```

### Results Model

```dart
class StudentResult {
  final String courseCode;
  final String courseTitle;
  final int? creditHours;
  final String? semester;
  final String? academicYear;
  final String? grade;
  final double? gradePoint;
  final double? marks;
  final String? status;
  final String? courseType;

  StudentResult({
    required this.courseCode,
    required this.courseTitle,
    this.creditHours,
    this.semester,
    this.academicYear,
    this.grade,
    this.gradePoint,
    this.marks,
    this.status,
    this.courseType,
  });

  factory StudentResult.fromJson(Map<String, dynamic> json) {
    return StudentResult(
      courseCode: json['course_code'] as String? ?? '',
      courseTitle: json['course_title'] as String? ?? '',
      creditHours: json['credit_hours'] as int?,
      semester: json['semester'] as String?,
      academicYear: json['academic_year'] as String?,
      grade: json['grade'] as String?,
      gradePoint: (json['grade_point'] as num?)?.toDouble(),
      marks: (json['marks'] as num?)?.toDouble(),
      status: json['status'] as String?,
      courseType: json['course_type'] as String?,
    );
  }

  bool get isPassed {
    if (gradePoint == null) return false;
    return gradePoint! >= 2.0;
  }
}

class ResultsSummary {
  final int totalCourses;
  final int passedCourses;
  final int failedCourses;
  final double averageGpa;
  final String academicStanding;

  ResultsSummary({
    required this.totalCourses,
    required this.passedCourses,
    required this.failedCourses,
    required this.averageGpa,
    required this.academicStanding,
  });

  double get passRate {
    if (totalCourses == 0) return 0;
    return passedCourses / totalCourses * 100;
  }
}
```

### Integration with Mental Health Analysis

The results can be used for mental health and academic performance correlation:

```dart
class MentalHealthAnalysisService {
  
  /// Analyzes academic performance impact on mental health
  static Map<String, dynamic> analyzeAcademicImpact(ResultsSummary summary) {
    final List<String> riskFactors = [];
    final List<String> recommendations = [];

    // Check for academic stress indicators
    if (summary.averageGpa < 2.0) {
      riskFactors.add('Low GPA below 2.0 - Academic probation risk');
      recommendations.add('Consider academic counseling support');
    }

    if (summary.passRate < 70) {
      riskFactors.add('Pass rate below 70%');
      recommendations.add('Review study habits and time management');
    }

    if (summary.failedCourses > 2) {
      riskFactors.add('Multiple failed courses');
      recommendations.add('Discuss course difficulty with advisors');
    }

    // Calculate overall risk level
    String riskLevel = 'LOW';
    if (riskFactors.length >= 3) {
      riskLevel = 'HIGH';
    } else if (riskFactors.isNotEmpty) {
      riskLevel = 'MODERATE';
    }

    return {
      'riskLevel': riskLevel,
      'riskFactors': riskFactors,
      'recommendations': recommendations,
      'academicSummary': {
        'gpa': summary.averageGpa,
        'passRate': summary.passRate,
        'failedCourses': summary.failedCourses,
      },
    };
  }

  /// Generates performance trend analysis
  static Map<String, dynamic> analyzeTrends(List<StudentResult> courses) {
    // Group by semester
    final Map<String, List<StudentResult>> bySemester = {};
    for (final course in courses) {
      final key = '${course.academicYear}_${course.semester}';
      bySemester.putIfAbsent(key, () => []).add(course);
    }

    // Calculate GPA per semester
    final trendData = <String, double>{};
    for (final entry in bySemester.entries) {
      final semesterGpa = entry.value
          .where((c) => c.gradePoint != null)
          .map((c) => c.gradePoint!)
          .fold(0.0, (a, b) => a + b) / entry.value.length;
      trendData[entry.key] = semesterGpa;
    }

    // Determine trend
    String trend = 'STABLE';
    if (trendData.length >= 2) {
      final values = trendData.values.toList();
      final recent = values[0];
      final previous = values[1];
      if (recent > previous + 0.2) {
        trend = 'IMPROVING';
      } else if (recent < previous - 0.2) {
        trend = 'DECLINING';
      }
    }

    return {
      'trend': trend,
      'semesterData': trendData,
      'analysis': _generateTrendAnalysis(trend, trendData),
    };
  }

  static String _generateTrendAnalysis(String trend, Map<String, double> data) {
    switch (trend) {
      case 'IMPROVING':
        return 'Academic performance is improving. Continue with current study strategies.';
      case 'DECLINING':
        return 'Academic performance is declining. Consider seeking academic support early.';
      default:
        return 'Academic performance is stable. Maintain consistent effort.';
    }
  }
}
```

## Offline Support

The backend caches results, allowing offline access:

```dart
class OfflineResultsCache {
  static const String _cacheKey = 'cached_results_';
  static const Duration _cacheValidity = Duration(hours: 24);

  /// Save results to local storage
  static Future<void> cacheResults(int clientId, Map<String, dynamic> data) async {
    final prefs = await SharedPreferences.getInstance();
    final cacheData = {
      'data': data,
      'timestamp': DateTime.now().toIso8601String(),
    };
    await prefs.setString('$_cacheKey$clientId', jsonEncode(cacheData));
  }

  /// Get cached results if valid
  static Future<Map<String, dynamic>?> getCachedResults(int clientId) async {
    final prefs = await SharedPreferences.getInstance();
    final cached = prefs.getString('$_cacheKey$clientId');
    
    if (cached == null) return null;
    
    try {
      final cacheData = jsonDecode(cached) as Map<String, dynamic>;
      final timestamp = DateTime.parse(cacheData['timestamp'] as String);
      
      // Check if cache is still valid
      if (DateTime.now().difference(timestamp) > _cacheValidity) {
        return null;
      }
      
      return Map<String, dynamic>.from(cacheData['data'] as Map);
    } catch (e) {
      return null;
    }
  }

  /// Clear cache
  static Future<void> clearCache(int clientId) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('$_cacheKey$clientId');
  }
}
```

## Usage Example

```dart
class ResultsScreen extends StatefulWidget {
  final int clientId;

  const ResultsScreen({Key? key, required this.clientId}) : super(key: key);

  @override
  State<ResultsScreen> createState() => _ResultsScreenState();
}

class _ResultsScreenState extends State<ResultsScreen> {
  bool _isLoading = false;
  Map<String, dynamic>? _results;
  Map<String, dynamic>? _error;

  @override
  void initState() {
    super.initState();
    _loadResults();
  }

  Future<void> _loadResults() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    // Try cached first
    final cached = await OfflineResultsCache.getCachedResults(widget.clientId);
    if (cached != null) {
      setState(() {
        _results = cached;
        _isLoading = false;
      });
      return;
    }

    // Fetch from backend
    final result = await ResultsService.fetchResultsForClient(
      clientId: widget.clientId,
    );

    if (result['success'] == true) {
      setState(() {
        _results = result['data'];
      });
      // Cache results
      await OfflineResultsCache.cacheResults(widget.clientId, _results!);
    } else {
      setState(() {
        _error = result;
      });
    }

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (_error != null) {
      return ErrorDisplay(error: _error!);
    }

    if (_results == null) {
      return const Center(child: Text('No results available'));
    }

    final summary = ResultsSummary(
      totalCourses: _results!['summary']['totalCourses'],
      passedCourses: _results!['summary']['passedCourses'],
      failedCourses: _results!['summary']['failedCourses'],
      averageGpa: _results!['summary']['averageGpa'].toDouble(),
      academicStanding: _results!['summary']['academicStanding'],
    );

    // Perform mental health analysis
    final analysis = MentalHealthAnalysisService.analyzeAcademicImpact(summary);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Academic Summary Card
          AcademicSummaryCard(summary: summary),
          
          const SizedBox(height: 16),
          
          // Mental Health Risk Assessment
          RiskAssessmentCard(analysis: analysis),
          
          const SizedBox(height: 16),
          
          // Performance Trend
          TrendCard(courses: _results!['courses']),
          
          const SizedBox(height: 16),
          
          // Course List
          CourseListWidget(courses: _results!['courses']),
        ],
      ),
    );
  }
}
```

## Configuration

Update your `pubspec.yaml` with required dependencies:

```yaml
dependencies:
  http: ^1.1.0
  shared_preferences: ^2.2.0
  flutter:
    sdk: flutter
```

## Error Handling

The service handles these error types:
- `network`: No internet connection
- `timeout`: Request timed out
- `auth`: Authentication failed
- `notFound`: Results not found
- `server`: Server error
- `format`: Invalid response format
- `unknown`: Unexpected error

## Next Steps

1. Run `flutter pub get` to install dependencies
2. Update API base URL in ResultsService
3. Implement the UI components
4. Test with your backend
5. Add unit tests for the analysis services
