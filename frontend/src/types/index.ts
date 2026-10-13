export type Role = 'STUDENT' | 'FACULTY' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';

export type SourcePlatform =
  | 'STUDYHUB'
  | 'YOUTUBE'
  | 'PW'
  | 'CODEWITHHARRY'
  | 'LEETCODE'
  | 'CODECHEF'
  | 'GFG'
  | 'NPTEL'
  | 'MIT_OCW'
  | 'OTHER';

export type ContentLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type ContentLanguage = 'ENGLISH' | 'HINDI' | 'OTHER';

export type ResourceType =
  | 'NOTE'
  | 'NOTES'
  | 'PDF'
  | 'VIDEO'
  | 'EXTERNAL_LINK'
  | 'PREVIOUS_YEAR_PAPER'
  | 'PREVIOUS_QUESTION_PAPER'
  | 'ASSIGNMENT'
  | 'STUDY_GUIDE'
  | 'PROJECT_RESOURCE'
  | 'YOUTUBE_LINK'
  | 'WEBSITE_URL';

export type ResourceStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'DELETED';
export type DoubtStatus = 'OPEN' | 'ANSWERED' | 'RESOLVED' | 'CLOSED';
export type NotificationType = 'RESOURCE_APPROVED' | 'RESOURCE_REJECTED' | 'NEW_ANSWER' | 'ANSWER_ACCEPTED' | 'RESOURCE_RATED' | 'SYSTEM_ALERT';

export type FocusMode = 'POMODORO' | 'STOPWATCH' | 'TIMER';
export type FocusStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  status: UserStatus;
  avatarUrl?: string;
  departmentId?: number;
  departmentName?: string;
  semester?: number;
  goals?: string;
  preferredLanguage?: ContentLanguage;
  level?: ContentLevel;
  reputationPoints?: number;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface Department {
  id: number;
  name: string;
  code: string;
  active?: boolean;
}

export interface Goal {
  id: number;
  departmentId?: number | null;
  goalKey: string;
  label: string;
  description?: string;
  active: boolean;
}

export interface DepartmentPlatform {
  id: number;
  departmentId: number;
  platform: SourcePlatform;
  priority: number;
}

export interface DepartmentTool {
  id: number;
  departmentId: number;
  name: string;
}

export interface DepartmentDetail {
  id: number;
  name: string;
  code: string;
  active: boolean;
  subjects: Subject[];
  goals: Goal[];
  platforms: DepartmentPlatform[];
  tools: DepartmentTool[];
}

export interface Subject {
  id: number;
  name: string;
  code: string;
  semester: number;
  departmentId: number;
  active?: boolean;
}

export interface Topic {
  id: number;
  name: string;
  subjectId: number;
}

export interface Tag {
  id: number;
  name: string;
  slug?: string;
}

export interface Resource {
  id: number;
  title: string;
  description: string;
  type: ResourceType;
  sourcePlatform: SourcePlatform;
  level: ContentLevel;
  language: ContentLanguage;
  isFree: boolean;
  bestFor?: string;
  verified: boolean;
  verifiedBy?: string;
  verifiedAt?: string;
  reliabilityScore: number;
  reliabilityLabel: string;
  filePath?: string;
  fileMimeType?: string;
  fileSizeBytes?: number;
  externalUrl?: string;
  departmentId?: number;
  departmentName?: string;
  subjectId?: number;
  subjectName?: string;
  topicId?: number;
  topicName?: string;
  semester: number;
  uploaderId: number;
  uploaderName: string;
  status: ResourceStatus;
  avgRating: number;
  ratingCount: number;
  usefulCount: number;
  viewCount: number;
  downloadCount: number;
  aiSummary?: string;
  aiKeyPoints?: string;
  aiRevisionNotes?: string;
  tags: string[];
  bookmarked?: boolean;
  userRating?: number;
  userVotedUseful?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ResourceRecommendation {
  resource: Resource;
  reason: string;
}

export interface Review {
  id: number;
  resourceId: number;
  userId: number;
  userName: string;
  userRole: Role;
  rating: number;
  text: string;
  helpfulCount: number;
  votedHelpfulByCurrentUser: boolean;
  createdAt: string;
}

export interface FocusSession {
  id: number;
  userId: number;
  userName: string;
  subjectId?: number;
  subjectName?: string;
  mode: FocusMode;
  plannedMinutes: number;
  actualMinutes: number;
  startedAt: string;
  endedAt?: string;
  status: FocusStatus;
}

export interface FocusStats {
  todayMinutes: number;
  thisWeekMinutes: number;
  totalMinutes: number;
  streakDays: number;
  completedSessionsCount: number;
  subjectBreakdownMinutes: Record<string, number>;
  activeSession?: FocusSession;
}

export interface FocusLeaderboardEntry {
  rank: number;
  userId: number;
  fullName: string;
  departmentName: string;
  totalMinutes: number;
  streakDays: number;
  sessionsCount: number;
}

export interface FocusRoomParticipant {
  userId: number;
  userName: string;
  avatar: string;
  status: 'FOCUSING' | 'ON_BREAK' | 'LEFT';
  currentSubject: string;
  elapsedMinutes: number;
  timestamp: number;
}

export interface Doubt {
  id: number;
  title: string;
  content: string;
  status: DoubtStatus;
  subjectName: string;
  departmentName?: string;
  authorId: number;
  authorName: string;
  upvotesCount: number;
  answersCount: number;
  acceptedAnswerId?: number;
  tags: string[];
  createdAt: string;
}

export interface Answer {
  id: number;
  doubtId: number;
  content: string;
  authorId: number;
  authorName: string;
  upvotesCount: number;
  isAccepted: boolean;
  hasVoted?: boolean;
  createdAt: string;
}

export interface NotificationItem {
  id: number;
  recipientId: number;
  title: string;
  message: string;
  type: NotificationType;
  targetUrl?: string;
  isRead: boolean;
  createdAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalResources: number;
  pendingResources: number;
  totalDoubts: number;
  totalReports: number;
  pendingReports: number;
  activeSessions?: number;
  totalDownloads?: number;
}

export interface WhiteboardSession {
  id: number;
  sessionCode: string;
  title: string;
  hostId: number;
  hostName: string;
  active: boolean;
  createdAt: string;
}

export interface WhiteboardDrawEvent {
  type: 'DRAW_LINE' | 'DRAW_SHAPE' | 'ADD_TEXT' | 'CLEAR_CANVAS' | 'CURSOR_MOVE' | 'SESSION_ENDED';
  sessionCode: string;
  userId: number;
  userName: string;
  sequenceId: number;
  payload: Record<string, any>;
  timestamp: number;
}

