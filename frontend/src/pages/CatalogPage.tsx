import { useState } from 'react';
import { api, ApiError } from '../api/client';
import { useSession } from '../context/SessionContext';
import SessionPanel from '../components/SessionPanel';
import type {
  ClassBatchResponse,
  ClassScheduleResponse,
  CourseResponse,
  StudentResponse,
} from '../api/types';

type Tab = 'courses' | 'students' | 'batches' | 'schedules';

const TABS: { id: Tab; label: string }[] = [
  { id: 'courses', label: 'Courses' },
  { id: 'students', label: 'Students' },
  { id: 'batches', label: 'Batches' },
  { id: 'schedules', label: 'Schedules' },
];

function describe(e: unknown): string {
  return e instanceof ApiError ? e.message : (e as Error).message;
}

type Status = { text: string; kind?: 'ok' | 'err' };

function StatusLine({ status }: { status: Status }) {
  if (!status.text) return null;
  return (
    <div
      style={{
        fontSize: 12,
        margin: '8px 0',
        color: status.kind === 'err' ? 'var(--danger)' : status.kind === 'ok' ? 'var(--success)' : 'var(--text-dim)',
      }}
    >
      {status.text}
    </div>
  );
}

const listStyle: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: 8 };
const cardStyle: React.CSSProperties = {
  border: '1px solid var(--border)',
  borderRadius: 8,
  padding: 10,
  background: 'var(--panel-2)',
};
const gridStyle: React.CSSProperties = { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 };

export default function CatalogPage({ onBack }: { onBack?: () => void }) {
  const s = useSession();
  const [tab, setTab] = useState<Tab>('courses');

  function requireToken(setStatus: (st: Status) => void): boolean {
    if (!s.token.trim()) {
      setStatus({ text: 'Get an ADMIN dev token first (role dropdown in the sidebar).', kind: 'err' });
      return false;
    }
    return true;
  }

  return (
    <div style={{ display: 'flex', height: '100%' }}>
      <SessionPanel showRole />

      <main style={{ flex: 1, padding: 20, overflowY: 'auto' }}>
        {onBack && (
          <button className="back" type="button" onClick={onBack} style={{ marginBottom: 16 }}>
            ← Back
          </button>
        )}
        <h2 style={{ fontSize: 15, marginTop: 0 }}>LMS catalog</h2>
        <p style={{ color: 'var(--text-dim)', fontSize: 13, maxWidth: 700 }}>
          Manage the real, MySQL-backed course/student/batch/schedule data the assistant's tools and RAG
          retrieval read from. Changes here are live immediately - no reindex needed for course/batch/schedule
          data used by <code>AssistantTools</code> (only FAQ text needs a reindex - see the Admin page).
        </p>

        <div style={{ display: 'flex', gap: 8, margin: '12px 0' }}>
          {TABS.map((t) => (
            <button
              key={t.id}
              className={tab === t.id ? '' : 'secondary'}
              type="button"
              onClick={() => setTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        {tab === 'courses' && <CoursesTab requireToken={requireToken} />}
        {tab === 'students' && <StudentsTab requireToken={requireToken} />}
        {tab === 'batches' && <BatchesTab requireToken={requireToken} />}
        {tab === 'schedules' && <SchedulesTab requireToken={requireToken} />}
      </main>
    </div>
  );
}

// ==================== COURSES ====================

const emptyCourseForm = {
  courseTitle: '',
  subjectNm: '',
  description: '',
  language: '',
  level: '',
  skills: '',
};

function CoursesTab({ requireToken }: { requireToken: (setStatus: (st: Status) => void) => boolean }) {
  const s = useSession();
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [form, setForm] = useState(emptyCourseForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [status, setStatus] = useState<Status>({ text: '' });

  async function load() {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Loading courses…' });
    try {
      const list = await api.listCourses(s.baseUrl, s.token);
      setCourses(list);
      setStatus({ text: `Loaded ${list.length} course(s).`, kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function submit() {
    if (!requireToken(setStatus)) return;
    if (!form.courseTitle.trim()) {
      setStatus({ text: 'Course title is required.', kind: 'err' });
      return;
    }
    const body = {
      courseTitle: form.courseTitle,
      subjectNm: form.subjectNm,
      description: form.description,
      language: form.language,
      level: form.level,
      skills: form.skills.split(',').map((s) => s.trim()).filter(Boolean),
    };
    setStatus({ text: editingId ? 'Updating course…' : 'Creating course…' });
    try {
      if (editingId) {
        await api.updateCourse(s.baseUrl, s.token, editingId, body);
      } else {
        await api.createCourse(s.baseUrl, s.token, body);
      }
      setForm(emptyCourseForm);
      setEditingId(null);
      await load();
      setStatus({ text: 'Saved.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function remove(courseId: string) {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Deleting…' });
    try {
      await api.deleteCourse(s.baseUrl, s.token, courseId);
      await load();
      setStatus({ text: 'Deleted.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={load}>Load courses</button>
      </div>
      <StatusLine status={status} />

      <div style={gridStyle}>
        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>
            {editingId ? `Editing ${editingId}` : 'New course'}
          </h3>
          <label>Title</label>
          <input value={form.courseTitle} onChange={(e) => setForm({ ...form, courseTitle: e.target.value })} />
          <label>Subject</label>
          <input value={form.subjectNm} onChange={(e) => setForm({ ...form, subjectNm: e.target.value })} />
          <label>Description</label>
          <textarea
            style={{ minHeight: 70 }}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <label>Language</label>
          <input value={form.language} onChange={(e) => setForm({ ...form, language: e.target.value })} />
          <label>Level</label>
          <input
            value={form.level}
            placeholder="BEGINNER / INTERMEDIATE / ADVANCED"
            onChange={(e) => setForm({ ...form, level: e.target.value })}
          />
          <label>Skills (comma-separated)</label>
          <input value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} />
          <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
            <button onClick={submit}>{editingId ? 'Update' : 'Create'}</button>
            {editingId && (
              <button
                className="secondary"
                onClick={() => {
                  setEditingId(null);
                  setForm(emptyCourseForm);
                }}
              >
                Cancel
              </button>
            )}
          </div>
        </div>

        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>Courses ({courses.length})</h3>
          <div style={listStyle}>
            {courses.map((c) => (
              <div key={c.courseId} style={cardStyle}>
                <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>
                  {c.courseId} · {c.subjectNm} · {c.level}
                </div>
                <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>{c.courseTitle}</div>
                <div style={{ fontSize: 12, color: 'var(--text-dim)', marginTop: 4 }}>{c.description}</div>
                <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                  <button
                    className="secondary"
                    onClick={() => {
                      setEditingId(c.courseId);
                      setForm({
                        courseTitle: c.courseTitle,
                        subjectNm: c.subjectNm ?? '',
                        description: c.description ?? '',
                        language: c.language ?? '',
                        level: c.level ?? '',
                        skills: (c.skills ?? []).join(', '),
                      });
                    }}
                  >
                    Edit
                  </button>
                  <button className="secondary" onClick={() => remove(c.courseId)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {courses.length === 0 && (
              <div style={{ color: 'var(--text-dim)', fontSize: 12 }}>No courses loaded yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// ==================== STUDENTS ====================

const emptyStudentForm = { firstName: '', lastName: '', email: '', phone: '' };

function StudentsTab({ requireToken }: { requireToken: (setStatus: (st: Status) => void) => boolean }) {
  const s = useSession();
  const [students, setStudents] = useState<StudentResponse[]>([]);
  const [form, setForm] = useState(emptyStudentForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [status, setStatus] = useState<Status>({ text: '' });

  async function load() {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Loading students…' });
    try {
      const list = await api.listStudents(s.baseUrl, s.token);
      setStudents(list);
      setStatus({ text: `Loaded ${list.length} student(s).`, kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function submit() {
    if (!requireToken(setStatus)) return;
    if (!form.firstName.trim() || !form.lastName.trim() || !form.email.trim()) {
      setStatus({ text: 'First name, last name, and email are required.', kind: 'err' });
      return;
    }
    setStatus({ text: editingId ? 'Updating student…' : 'Registering student…' });
    try {
      if (editingId) {
        await api.updateStudent(s.baseUrl, s.token, editingId, form);
      } else {
        await api.createStudent(s.baseUrl, s.token, form);
      }
      setForm(emptyStudentForm);
      setEditingId(null);
      await load();
      setStatus({ text: 'Saved.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function remove(studentId: string) {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Deleting…' });
    try {
      await api.deleteStudent(s.baseUrl, s.token, studentId);
      await load();
      setStatus({ text: 'Deleted.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={load}>Load students</button>
      </div>
      <StatusLine status={status} />

      <div style={gridStyle}>
        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>
            {editingId ? `Editing ${editingId}` : 'New student'}
          </h3>
          <label>First name</label>
          <input value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
          <label>Last name</label>
          <input value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
          <label>Email</label>
          <input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <label>Phone</label>
          <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
            <button onClick={submit}>{editingId ? 'Update' : 'Create'}</button>
            {editingId && (
              <button
                className="secondary"
                onClick={() => {
                  setEditingId(null);
                  setForm(emptyStudentForm);
                }}
              >
                Cancel
              </button>
            )}
          </div>
        </div>

        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>Students ({students.length})</h3>
          <div style={listStyle}>
            {students.map((st) => (
              <div key={st.studentId} style={cardStyle}>
                <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>{st.studentId}</div>
                <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>
                  {st.firstNm} {st.lastNm}
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-dim)', marginTop: 4 }}>
                  {st.emailId} {st.mobileNum ? `· ${st.mobileNum}` : ''}
                </div>
                <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                  <button
                    className="secondary"
                    onClick={() => {
                      setEditingId(st.studentId);
                      setForm({
                        firstName: st.firstNm,
                        lastName: st.lastNm,
                        email: st.emailId,
                        phone: st.mobileNum ?? '',
                      });
                    }}
                  >
                    Edit
                  </button>
                  <button className="secondary" onClick={() => remove(st.studentId)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {students.length === 0 && (
              <div style={{ color: 'var(--text-dim)', fontSize: 12 }}>No students loaded yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// ==================== BATCHES ====================

const emptyBatchForm = {
  courseId: '',
  className: '',
  startDate: '',
  endDate: '',
  status: 'ACTIVE',
  capacity: '',
};

function BatchesTab({ requireToken }: { requireToken: (setStatus: (st: Status) => void) => boolean }) {
  const s = useSession();
  const [batches, setBatches] = useState<ClassBatchResponse[]>([]);
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [form, setForm] = useState(emptyBatchForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [status, setStatus] = useState<Status>({ text: '' });

  async function load() {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Loading batches…' });
    try {
      const [batchList, courseList] = await Promise.all([
        api.listBatches(s.baseUrl, s.token),
        api.listCourses(s.baseUrl, s.token),
      ]);
      setBatches(batchList);
      setCourses(courseList);
      setStatus({ text: `Loaded ${batchList.length} batch(es).`, kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function submit() {
    if (!requireToken(setStatus)) return;
    if (!form.courseId.trim() || !form.className.trim()) {
      setStatus({ text: 'Course and batch name are required.', kind: 'err' });
      return;
    }
    const body = {
      courseId: form.courseId,
      className: form.className,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      status: form.status,
      capacity: form.capacity ? Number(form.capacity) : null,
    };
    setStatus({ text: editingId ? 'Updating batch…' : 'Creating batch…' });
    try {
      if (editingId) {
        await api.updateBatch(s.baseUrl, s.token, editingId, body);
      } else {
        await api.createBatch(s.baseUrl, s.token, body);
      }
      setForm(emptyBatchForm);
      setEditingId(null);
      await load();
      setStatus({ text: 'Saved.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function remove(id: number) {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Deleting…' });
    try {
      await api.deleteBatch(s.baseUrl, s.token, id);
      await load();
      setStatus({ text: 'Deleted.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={load}>Load batches</button>
      </div>
      <StatusLine status={status} />

      <div style={gridStyle}>
        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>
            {editingId ? `Editing batch #${editingId}` : 'New batch'}
          </h3>
          <label>Course</label>
          <select value={form.courseId} onChange={(e) => setForm({ ...form, courseId: e.target.value })}>
            <option value="">Select a course…</option>
            {courses.map((c) => (
              <option key={c.courseId} value={c.courseId}>
                {c.courseId} — {c.courseTitle}
              </option>
            ))}
          </select>
          <label>Batch name</label>
          <input value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} />
          <label>Start date</label>
          <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
          <label>End date</label>
          <input type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
          <label>Status</label>
          <input value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} />
          <label>Capacity</label>
          <input
            type="number"
            value={form.capacity}
            onChange={(e) => setForm({ ...form, capacity: e.target.value })}
          />
          <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
            <button onClick={submit}>{editingId ? 'Update' : 'Create'}</button>
            {editingId && (
              <button
                className="secondary"
                onClick={() => {
                  setEditingId(null);
                  setForm(emptyBatchForm);
                }}
              >
                Cancel
              </button>
            )}
          </div>
        </div>

        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>Batches ({batches.length})</h3>
          <div style={listStyle}>
            {batches.map((b) => (
              <div key={b.id} style={cardStyle}>
                <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>
                  #{b.id} · {b.courseId} · {b.status}
                </div>
                <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>{b.className}</div>
                <div style={{ fontSize: 12, color: 'var(--text-dim)', marginTop: 4 }}>
                  {b.startDate ?? '?'} → {b.endDate ?? '?'} · {b.seatsTaken}/{b.capacity ?? '∞'} seats
                </div>
                <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                  <button
                    className="secondary"
                    onClick={() => {
                      setEditingId(b.id);
                      setForm({
                        courseId: b.courseId,
                        className: b.className,
                        startDate: b.startDate ?? '',
                        endDate: b.endDate ?? '',
                        status: b.status ?? 'ACTIVE',
                        capacity: b.capacity != null ? String(b.capacity) : '',
                      });
                    }}
                  >
                    Edit
                  </button>
                  <button className="secondary" onClick={() => remove(b.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {batches.length === 0 && (
              <div style={{ color: 'var(--text-dim)', fontSize: 12 }}>No batches loaded yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// ==================== SCHEDULES ====================

const emptyScheduleForm = {
  classBatchId: '',
  className: '',
  classDate: '',
  startTime: '',
  endTime: '',
  mode: 'ONLINE',
  status: 'SCHEDULED',
};

function SchedulesTab({ requireToken }: { requireToken: (setStatus: (st: Status) => void) => boolean }) {
  const s = useSession();
  const [schedules, setSchedules] = useState<ClassScheduleResponse[]>([]);
  const [batches, setBatches] = useState<ClassBatchResponse[]>([]);
  const [form, setForm] = useState(emptyScheduleForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [status, setStatus] = useState<Status>({ text: '' });

  async function load() {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Loading schedules…' });
    try {
      const [scheduleList, batchList] = await Promise.all([
        api.listSchedules(s.baseUrl, s.token),
        api.listBatches(s.baseUrl, s.token),
      ]);
      setSchedules(scheduleList);
      setBatches(batchList);
      setStatus({ text: `Loaded ${scheduleList.length} schedule(s).`, kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function submit() {
    if (!requireToken(setStatus)) return;
    if (!form.classBatchId) {
      setStatus({ text: 'A batch is required.', kind: 'err' });
      return;
    }
    const body = {
      classBatchId: Number(form.classBatchId),
      className: form.className,
      classDate: form.classDate || null,
      startTime: form.startTime || null,
      endTime: form.endTime || null,
      mode: form.mode,
      status: form.status,
    };
    setStatus({ text: editingId ? 'Updating schedule…' : 'Creating schedule…' });
    try {
      if (editingId) {
        await api.updateSchedule(s.baseUrl, s.token, editingId, body);
      } else {
        await api.createSchedule(s.baseUrl, s.token, body);
      }
      setForm(emptyScheduleForm);
      setEditingId(null);
      await load();
      setStatus({ text: 'Saved.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  async function remove(id: number) {
    if (!requireToken(setStatus)) return;
    setStatus({ text: 'Deleting…' });
    try {
      await api.deleteSchedule(s.baseUrl, s.token, id);
      await load();
      setStatus({ text: 'Deleted.', kind: 'ok' });
    } catch (e) {
      setStatus({ text: describe(e), kind: 'err' });
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 8 }}>
        <button onClick={load}>Load schedules</button>
      </div>
      <StatusLine status={status} />

      <div style={gridStyle}>
        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>
            {editingId ? `Editing schedule #${editingId}` : 'New schedule'}
          </h3>
          <label>Batch</label>
          <select
            value={form.classBatchId}
            onChange={(e) => setForm({ ...form, classBatchId: e.target.value })}
          >
            <option value="">Select a batch…</option>
            {batches.map((b) => (
              <option key={b.id} value={b.id}>
                #{b.id} — {b.className}
              </option>
            ))}
          </select>
          <label>Class name</label>
          <input value={form.className} onChange={(e) => setForm({ ...form, className: e.target.value })} />
          <label>Date</label>
          <input type="date" value={form.classDate} onChange={(e) => setForm({ ...form, classDate: e.target.value })} />
          <label>Start time</label>
          <input type="time" value={form.startTime} onChange={(e) => setForm({ ...form, startTime: e.target.value })} />
          <label>End time</label>
          <input type="time" value={form.endTime} onChange={(e) => setForm({ ...form, endTime: e.target.value })} />
          <label>Mode</label>
          <input value={form.mode} onChange={(e) => setForm({ ...form, mode: e.target.value })} />
          <label>Status</label>
          <input value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })} />
          <div style={{ display: 'flex', gap: 8, marginTop: 10 }}>
            <button onClick={submit}>{editingId ? 'Update' : 'Create'}</button>
            {editingId && (
              <button
                className="secondary"
                onClick={() => {
                  setEditingId(null);
                  setForm(emptyScheduleForm);
                }}
              >
                Cancel
              </button>
            )}
          </div>
        </div>

        <div>
          <h3 style={{ fontSize: 13, color: 'var(--text-dim)' }}>Schedules ({schedules.length})</h3>
          <div style={listStyle}>
            {schedules.map((sc) => (
              <div key={sc.id} style={cardStyle}>
                <div style={{ fontSize: 11, color: 'var(--text-dim)' }}>
                  #{sc.id} · Batch #{sc.classBatchId} · {sc.status}
                </div>
                <div style={{ fontSize: 13, fontWeight: 600, marginTop: 4 }}>{sc.className}</div>
                <div style={{ fontSize: 12, color: 'var(--text-dim)', marginTop: 4 }}>
                  {sc.classDate ?? '?'} · {sc.startTime ?? '?'}-{sc.endTime ?? '?'} · {sc.mode}
                </div>
                <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
                  <button
                    className="secondary"
                    onClick={() => {
                      setEditingId(sc.id);
                      setForm({
                        classBatchId: String(sc.classBatchId),
                        className: sc.className,
                        classDate: sc.classDate ?? '',
                        startTime: sc.startTime ?? '',
                        endTime: sc.endTime ?? '',
                        mode: sc.mode ?? 'ONLINE',
                        status: sc.status ?? 'SCHEDULED',
                      });
                    }}
                  >
                    Edit
                  </button>
                  <button className="secondary" onClick={() => remove(sc.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {schedules.length === 0 && (
              <div style={{ color: 'var(--text-dim)', fontSize: 12 }}>No schedules loaded yet.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
