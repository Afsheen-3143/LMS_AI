export interface AssistantChatRequest {
  message: string;
  conversationId: string | null;
}

export interface AssistantChatResponse {
  response: string;
  conversationId: string;
  actionRequired?: string | null;
  pendingAction?: Record<string, unknown> | null;
}

export interface ChatMessageResponse {
  role: string;
  content: string;
  timestamp: string;
}

export interface EnrollmentConfirmRequest {
  courseId: string | null;
  programId: string | null;
  batchId: number | null;
}

export interface EnrollmentConfirmResponse {
  success: boolean;
  message: string;
  enrollmentId?: number | null;
}

export interface AssistantFaqRequest {
  category: string;
  question: string;
  answer: string;
  active: boolean;
}

export interface AssistantFaqResponse {
  id: number;
  category: string;
  question: string;
  answer: string;
  active: boolean;
}

export interface IngestionResult {
  [key: string]: unknown;
}

export interface DevTokenResponse {
  token: string;
  studentId: string;
  role: string;
}

export interface StudentRegistrationRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export interface StudentResponse {
  studentId: string;
  firstNm: string;
  lastNm: string;
  emailId: string;
  mobileNum: string;
}

export type ChatRole = 'user' | 'assistant' | 'system';

export interface ChatUiMessage {
  id: string;
  role: ChatRole;
  content: string;
  actionRequired?: string | null;
  pendingAction?: Record<string, unknown> | null;
}

export interface CourseRequest {
  courseTitle: string;
  subjectNm: string;
  description: string;
  language: string;
  level: string;
  skills: string[];
}

export interface CourseResponse {
  courseId: string;
  courseTitle: string;
  description: string;
  language: string;
  skills: string[];
  subjectNm: string;
  level: string;
}

export interface ProgramResponse {
  programId: string;
  programTitle: string;
  description: string;
  coursesList: CourseResponse[];
}

export interface ClassBatchRequest {
  courseId: string;
  className: string;
  startDate: string | null;
  endDate: string | null;
  status: string;
  capacity: number | null;
}

export interface ClassBatchResponse {
  id: number;
  courseId: string;
  courseTitle: string;
  className: string;
  startDate: string | null;
  endDate: string | null;
  status: string;
  capacity: number | null;
  seatsTaken: number;
}

export interface ClassScheduleRequest {
  classBatchId: number;
  className: string;
  classDate: string | null;
  startTime: string | null;
  endTime: string | null;
  mode: string;
  status: string;
}

export interface ClassScheduleResponse {
  id: number;
  classBatchId: number;
  className: string;
  classDate: string | null;
  startTime: string | null;
  endTime: string | null;
  mode: string;
  status: string;
}
