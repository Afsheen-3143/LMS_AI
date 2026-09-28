import type {
  AssistantChatRequest,
  AssistantChatResponse,
  AssistantFaqRequest,
  AssistantFaqResponse,
  ChatMessageResponse,
  ClassBatchRequest,
  ClassBatchResponse,
  ClassScheduleRequest,
  ClassScheduleResponse,
  CourseRequest,
  CourseResponse,
  DevTokenResponse,
  EnrollmentConfirmRequest,
  EnrollmentConfirmResponse,
  IngestionResult,
  ProgramResponse,
  StudentRegistrationRequest,
  StudentResponse,
} from './types';

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function request<T>(
  baseUrl: string,
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const res = await fetch(baseUrl.replace(/\/$/, '') + path, options);
  if (!res.ok) {
    let body = '';
    try {
      body = await res.text();
    } catch {
      // ignore
    }
    throw new ApiError(res.status, `HTTP ${res.status}${body ? ' - ' + body : ''}`);
  }
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

function authHeaders(token: string): HeadersInit {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

export const api = {
  ping(baseUrl: string) {
    return fetch(baseUrl.replace(/\/$/, '') + '/v3/api-docs');
  },

  getDevToken(baseUrl: string, studentId: string, role: string) {
    return request<DevTokenResponse>(
      baseUrl,
      `/api/dev/token/${encodeURIComponent(studentId)}?role=${encodeURIComponent(role)}`,
      {},
    );
  },

  registerStudent(baseUrl: string, body: StudentRegistrationRequest) {
    return request<StudentResponse>(baseUrl, '/api/dev/students/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  },

  chat(baseUrl: string, token: string, body: AssistantChatRequest) {
    return request<AssistantChatResponse>(baseUrl, '/api/student-assistant/chat', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  getHistory(baseUrl: string, token: string, sessionId: string) {
    return request<ChatMessageResponse[]>(
      baseUrl,
      `/api/student-assistant/history/${encodeURIComponent(sessionId)}`,
      { headers: authHeaders(token) },
    );
  },

  confirmEnrollment(baseUrl: string, token: string, body: EnrollmentConfirmRequest) {
    return request<EnrollmentConfirmResponse>(
      baseUrl,
      '/api/student-assistant/confirm-enrollment',
      {
        method: 'POST',
        headers: authHeaders(token),
        body: JSON.stringify(body),
      },
    );
  },

  listFaqs(baseUrl: string, token: string) {
    return request<AssistantFaqResponse[]>(baseUrl, '/api/admin/assistant/faqs', {
      headers: authHeaders(token),
    });
  },

  createFaq(baseUrl: string, token: string, body: AssistantFaqRequest) {
    return request<AssistantFaqResponse>(baseUrl, '/api/admin/assistant/faqs', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  updateFaq(baseUrl: string, token: string, id: number, body: AssistantFaqRequest) {
    return request<AssistantFaqResponse>(baseUrl, `/api/admin/assistant/faqs/${id}`, {
      method: 'PUT',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  deleteFaq(baseUrl: string, token: string, id: number) {
    return request<void>(baseUrl, `/api/admin/assistant/faqs/${id}`, {
      method: 'DELETE',
      headers: authHeaders(token),
    });
  },

  reindex(baseUrl: string, token: string) {
    return request<IngestionResult>(baseUrl, '/api/admin/assistant/reindex', {
      method: 'POST',
      headers: authHeaders(token),
    });
  },

  // ---- LMS catalog: courses ----

  listCourses(baseUrl: string, token: string) {
    return request<CourseResponse[]>(baseUrl, '/api/admin/lms/courses', { headers: authHeaders(token) });
  },

  createCourse(baseUrl: string, token: string, body: CourseRequest) {
    return request<CourseResponse>(baseUrl, '/api/admin/lms/courses', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  updateCourse(baseUrl: string, token: string, courseId: string, body: CourseRequest) {
    return request<CourseResponse>(baseUrl, `/api/admin/lms/courses/${encodeURIComponent(courseId)}`, {
      method: 'PUT',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  deleteCourse(baseUrl: string, token: string, courseId: string) {
    return request<void>(baseUrl, `/api/admin/lms/courses/${encodeURIComponent(courseId)}`, {
      method: 'DELETE',
      headers: authHeaders(token),
    });
  },

  // ---- LMS catalog: programs (read-only) ----

  listPrograms(baseUrl: string, token: string) {
    return request<ProgramResponse[]>(baseUrl, '/api/admin/lms/programs', { headers: authHeaders(token) });
  },

  // ---- LMS catalog: students ----

  listStudents(baseUrl: string, token: string) {
    return request<StudentResponse[]>(baseUrl, '/api/admin/lms/students', { headers: authHeaders(token) });
  },

  createStudent(baseUrl: string, token: string, body: StudentRegistrationRequest) {
    return request<StudentResponse>(baseUrl, '/api/admin/lms/students', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  updateStudent(baseUrl: string, token: string, studentId: string, body: StudentRegistrationRequest) {
    return request<StudentResponse>(baseUrl, `/api/admin/lms/students/${encodeURIComponent(studentId)}`, {
      method: 'PUT',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  deleteStudent(baseUrl: string, token: string, studentId: string) {
    return request<void>(baseUrl, `/api/admin/lms/students/${encodeURIComponent(studentId)}`, {
      method: 'DELETE',
      headers: authHeaders(token),
    });
  },

  // ---- LMS catalog: batches ----

  listBatches(baseUrl: string, token: string) {
    return request<ClassBatchResponse[]>(baseUrl, '/api/admin/lms/batches', { headers: authHeaders(token) });
  },

  createBatch(baseUrl: string, token: string, body: ClassBatchRequest) {
    return request<ClassBatchResponse>(baseUrl, '/api/admin/lms/batches', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  updateBatch(baseUrl: string, token: string, id: number, body: ClassBatchRequest) {
    return request<ClassBatchResponse>(baseUrl, `/api/admin/lms/batches/${id}`, {
      method: 'PUT',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  deleteBatch(baseUrl: string, token: string, id: number) {
    return request<void>(baseUrl, `/api/admin/lms/batches/${id}`, {
      method: 'DELETE',
      headers: authHeaders(token),
    });
  },

  // ---- LMS catalog: schedules ----

  listSchedules(baseUrl: string, token: string) {
    return request<ClassScheduleResponse[]>(baseUrl, '/api/admin/lms/schedules', { headers: authHeaders(token) });
  },

  createSchedule(baseUrl: string, token: string, body: ClassScheduleRequest) {
    return request<ClassScheduleResponse>(baseUrl, '/api/admin/lms/schedules', {
      method: 'POST',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  updateSchedule(baseUrl: string, token: string, id: number, body: ClassScheduleRequest) {
    return request<ClassScheduleResponse>(baseUrl, `/api/admin/lms/schedules/${id}`, {
      method: 'PUT',
      headers: authHeaders(token),
      body: JSON.stringify(body),
    });
  },

  deleteSchedule(baseUrl: string, token: string, id: number) {
    return request<void>(baseUrl, `/api/admin/lms/schedules/${id}`, {
      method: 'DELETE',
      headers: authHeaders(token),
    });
  },
};
