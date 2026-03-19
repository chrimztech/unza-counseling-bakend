import React, { useState, useEffect } from 'react';
import { useAcademicPerformance } from '../../hooks/useAcademicPerformance';
import { StudentCourseHistory } from '../../models/academicPerformance';

const StudentResultsAnalysisScreen: React.FC = () => {
  const [studentId, setStudentId] = useState('');
  const [token, setToken] = useState('');
  const [searchAttempted, setSearchAttempted] = useState(false);

  const {
    studentResults,
    loadingStudentResults,
    studentResultsError,
    filteredCourses,
    selectedYear,
    selectedSession,
    fetchStudentResults,
    filterCourses,
    getAvailableYears,
    getAvailableSessions,
    resetFilters
  } = useAcademicPerformance();

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    setSearchAttempted(true);

    if (studentId && token) {
      await fetchStudentResults(studentId, token);
    }
  };

  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const year = e.target.value;
    filterCourses(year, '');
  };

  const handleSessionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const session = e.target.value;
    filterCourses(selectedYear, session);
  };

  const renderCourseTable = () => {
    if (!filteredCourses || filteredCourses.length === 0) {
      return (
        <div className="text-center py-12">
          <p className="text-gray-500">No courses found</p>
        </div>
      );
    }

    return (
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Course Code
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Course Description
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Credits
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Session
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Grade
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Grade Point
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {filteredCourses.map((course, index) => (
              <tr key={index} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {course.Course?.course_code}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {course.Course?.course_description}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {course.Course?.credits}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {course.Session?.session_code}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                    course.Grades?.grade_code ? getGradeColor(course.Grades.grade_code) : 'bg-gray-100 text-gray-800'
                  }`}>
                    {course.Grades?.grade_code || course.StudentCourse?.tmp_final_grade || 'N/A'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {course.Grades?.gradepoint || 'N/A'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  };

  const getGradeColor = (grade: string) => {
    if (!grade || grade === 'IN' || grade === '') {
      return 'bg-gray-100 text-gray-800';
    }

    switch (grade.toUpperCase()) {
      case 'A':
      case 'A+':
        return 'bg-green-100 text-green-800';
      case 'B':
      case 'B+':
        return 'bg-blue-100 text-blue-800';
      case 'C':
      case 'C+':
        return 'bg-yellow-100 text-yellow-800';
      case 'D':
      case 'D+':
        return 'bg-orange-100 text-orange-800';
      case 'F':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const renderStatistics = () => {
    if (!studentResults || !studentResults.summary) {
      return null;
    }

    const { summary } = studentResults;

    return (
      <div className="bg-white rounded-lg shadow p-6 mb-8">
        <h2 className="text-xl font-semibold mb-4 text-gray-900">Academic Statistics</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{summary.totalCourses}</div>
            <div className="text-sm text-gray-600">Total Courses</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">{summary.passedCourses}</div>
            <div className="text-sm text-gray-600">Passed</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">{summary.failedCourses}</div>
            <div className="text-sm text-gray-600">Failed</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-yellow-600">{summary.incompleteCourses}</div>
            <div className="text-sm text-gray-600">Incomplete</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {summary.totalCreditsAttempted || 0}
            </div>
            <div className="text-sm text-gray-600">Credits Attempted</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-indigo-600">
              {summary.averageGpa?.toFixed(2) || '0.00'}
            </div>
            <div className="text-sm text-gray-600">Average GPA</div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gray-100 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Student Results Analysis</h1>
          <p className="mt-2 text-sm text-gray-600">
            Search for and analyze student academic performance using SIS results
          </p>
        </div>

        {/* Search Form */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <form onSubmit={handleSearch} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="studentId" className="block text-sm font-medium text-gray-700 mb-2">
                  Student ID
                </label>
                <input
                  type="text"
                  id="studentId"
                  value={studentId}
                  onChange={(e) => setStudentId(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter student ID"
                />
                {searchAttempted && !studentId && (
                  <p className="mt-2 text-sm text-red-600">Student ID is required</p>
                )}
              </div>
              <div>
                <label htmlFor="token" className="block text-sm font-medium text-gray-700 mb-2">
                  Token
                </label>
                <input
                  type="text"
                  id="token"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter token"
                />
                {searchAttempted && !token && (
                  <p className="mt-2 text-sm text-red-600">Token is required</p>
                )}
              </div>
            </div>
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={loadingStudentResults}
                className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loadingStudentResults ? 'Searching...' : 'Search'}
              </button>
            </div>
          </form>
        </div>

        {/* Error Message */}
        {studentResultsError && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 mb-8">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{studentResultsError}</p>
              </div>
            </div>
          </div>
        )}

        {/* Results */}
        {studentResults && (
          <>
            {/* Statistics */}
            {renderStatistics()}

            {/* Filters */}
            <div className="bg-white rounded-lg shadow p-6 mb-8">
              <h3 className="text-lg font-semibold mb-4 text-gray-900">Filters</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="year" className="block text-sm font-medium text-gray-700 mb-2">
                    Year
                  </label>
                  <select
                    id="year"
                    value={selectedYear}
                    onChange={handleYearChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="">All Years</option>
                    {getAvailableYears().map(year => (
                      <option key={year} value={year}>
                        {year}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label htmlFor="session" className="block text-sm font-medium text-gray-700 mb-2">
                    Session
                  </label>
                  <select
                    id="session"
                    value={selectedSession}
                    onChange={handleSessionChange}
                    disabled={!selectedYear}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 disabled:opacity-50"
                  >
                    <option value="">All Sessions</option>
                    {getAvailableSessions(selectedYear).map(session => (
                      <option key={session} value={session}>
                        {session}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="mt-4">
                <button
                  type="button"
                  onClick={resetFilters}
                  className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
                >
                  Reset Filters
                </button>
              </div>
            </div>

            {/* Course Table */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-semibold mb-4 text-gray-900">Course History</h3>
              {renderCourseTable()}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default StudentResultsAnalysisScreen;
