import React, { useState, useRef, useEffect } from 'react';
import { 
  PenTool, 
  Square, 
  Circle, 
  Minus, 
  Eraser, 
  Trash2, 
  Save, 
  Download, 
  Users, 
  LogOut, 
  Plus, 
  Sparkles,
  RefreshCw,
  Share2,
  Lock
} from 'lucide-react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../services/api';
import { WhiteboardSession, WhiteboardDrawEvent } from '../types';
import { useAuth } from '../context/AuthContext';
import { getWebSocketUrl } from '../services/websocket';

type ToolType = 'pen' | 'eraser' | 'rectangle' | 'circle' | 'line';

export const WhiteboardPage: React.FC = () => {
  const { user, isAuthenticated } = useAuth();
  
  // Room state
  const [currentSession, setCurrentSession] = useState<WhiteboardSession | null>(null);
  const [sessionCodeInput, setSessionCodeInput] = useState('');
  const [newTitle, setNewTitle] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [isJoining, setIsJoining] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  
  // Canvas & Tools
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const ctxRef = useRef<CanvasRenderingContext2D | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [tool, setTool] = useState<ToolType>('pen');
  const [strokeColor, setStrokeColor] = useState('#6366f1');
  const [strokeWidth, setStrokeWidth] = useState(3);
  const [startPos, setStartPos] = useState<{ x: number; y: number } | null>(null);
  const [snapshotImg, setSnapshotImg] = useState<ImageData | null>(null);

  // STOMP client
  const stompClientRef = useRef<Client | null>(null);

  // Initialize Canvas
  useEffect(() => {
    if (!currentSession || !canvasRef.current) return;
    const canvas = canvasRef.current;
    canvas.width = canvas.parentElement?.clientWidth || 1000;
    canvas.height = 650;

    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      ctx.fillStyle = '#0f172a';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctxRef.current = ctx;
    }

    // Connect STOMP for live collaboration
    const token = localStorage.getItem('studyhub_access_token');
    const SockJSClass = (SockJS as any).default || SockJS;
    const client = new Client({
      webSocketFactory: () => new SockJSClass(getWebSocketUrl('/ws-studyhub')),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 3000,
    });

    client.onConnect = () => {
      client.subscribe(`/topic/whiteboard.${currentSession.sessionCode}`, (message) => {
        try {
          const event: WhiteboardDrawEvent = JSON.parse(message.body);
          if (event.userId !== user?.id) {
            handleRemoteDrawEvent(event);
          }
        } catch (e) {
          console.error('Remote draw event parse error', e);
        }
      });
    };

    client.activate();
    stompClientRef.current = client;

    // Load latest snapshot if exists
    api.get(`/whiteboard/sessions/${currentSession.sessionCode}/snapshots/latest`)
      .then((res) => {
        if (res.data.data?.stateJson && ctxRef.current) {
          const img = new Image();
          img.onload = () => {
            ctxRef.current?.drawImage(img, 0, 0);
          };
          img.src = res.data.data.stateJson;
        }
      })
      .catch(() => {});

    return () => {
      client.deactivate();
    };
  }, [currentSession]);

  const handleRemoteDrawEvent = (event: WhiteboardDrawEvent) => {
    const ctx = ctxRef.current;
    if (!ctx) return;

    if (event.type === 'DRAW_LINE') {
      const { x0, y0, x1, y1, color, width } = event.payload;
      ctx.strokeStyle = color;
      ctx.lineWidth = width;
      ctx.beginPath();
      ctx.moveTo(x0, y0);
      ctx.lineTo(x1, y1);
      ctx.stroke();
    } else if (event.type === 'DRAW_SHAPE') {
      const { shape, x, y, w, h, color, width } = event.payload;
      ctx.strokeStyle = color;
      ctx.lineWidth = width;
      ctx.beginPath();
      if (shape === 'rectangle') {
        ctx.strokeRect(x, y, w, h);
      } else if (shape === 'circle') {
        const radius = Math.sqrt(w * w + h * h) / 2;
        ctx.arc(x + w / 2, y + h / 2, radius, 0, 2 * Math.PI);
        ctx.stroke();
      } else if (shape === 'line') {
        ctx.moveTo(x, y);
        ctx.lineTo(x + w, y + h);
        ctx.stroke();
      }
    } else if (event.type === 'CLEAR_CANVAS') {
      if (canvasRef.current) {
        ctx.fillStyle = '#0f172a';
        ctx.fillRect(0, 0, canvasRef.current.width, canvasRef.current.height);
      }
    } else if (event.type === 'SESSION_ENDED') {
      alert('The session was ended by the host.');
      setCurrentSession(null);
    }
  };

  const broadcastEvent = (type: string, payload: Record<string, any>) => {
    if (!stompClientRef.current || !currentSession || !user) return;
    const drawEvent: WhiteboardDrawEvent = {
      type: type as any,
      sessionCode: currentSession.sessionCode,
      userId: user.id,
      userName: user.fullName,
      sequenceId: Date.now(),
      payload,
      timestamp: Date.now(),
    };
    stompClientRef.current.publish({
      destination: '/app/whiteboard.draw',
      body: JSON.stringify(drawEvent),
    });
  };

  // Drawing Handlers
  const startDrawing = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas || !ctxRef.current) return;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    setIsDrawing(true);
    setStartPos({ x, y });

    if (tool === 'pen' || tool === 'eraser') {
      ctxRef.current.strokeStyle = tool === 'eraser' ? '#0f172a' : strokeColor;
      ctxRef.current.lineWidth = tool === 'eraser' ? strokeWidth * 4 : strokeWidth;
      ctxRef.current.beginPath();
      ctxRef.current.moveTo(x, y);
    } else {
      setSnapshotImg(ctxRef.current.getImageData(0, 0, canvas.width, canvas.height));
    }
  };

  const draw = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!isDrawing || !ctxRef.current || !canvasRef.current) return;
    const rect = canvasRef.current.getBoundingClientRect();
    const currentX = e.clientX - rect.left;
    const currentY = e.clientY - rect.top;

    const ctx = ctxRef.current;

    if (tool === 'pen' || tool === 'eraser') {
      ctx.lineTo(currentX, currentY);
      ctx.stroke();

      if (startPos) {
        broadcastEvent('DRAW_LINE', {
          x0: startPos.x,
          y0: startPos.y,
          x1: currentX,
          y1: currentY,
          color: tool === 'eraser' ? '#0f172a' : strokeColor,
          width: tool === 'eraser' ? strokeWidth * 4 : strokeWidth,
        });
      }
      setStartPos({ x: currentX, y: currentY });
    } else if (snapshotImg && startPos) {
      // Restore before drawing shape preview
      ctx.putImageData(snapshotImg, 0, 0);
      ctx.strokeStyle = strokeColor;
      ctx.lineWidth = strokeWidth;
      ctx.beginPath();

      const w = currentX - startPos.x;
      const h = currentY - startPos.y;

      if (tool === 'rectangle') {
        ctx.strokeRect(startPos.x, startPos.y, w, h);
      } else if (tool === 'circle') {
        const radius = Math.sqrt(w * w + h * h) / 2;
        ctx.arc(startPos.x + w / 2, startPos.y + h / 2, radius, 0, 2 * Math.PI);
        ctx.stroke();
      } else if (tool === 'line') {
        ctx.moveTo(startPos.x, startPos.y);
        ctx.lineTo(currentX, currentY);
        ctx.stroke();
      }
    }
  };

  const stopDrawing = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!isDrawing) return;
    setIsDrawing(false);

    if (tool !== 'pen' && tool !== 'eraser' && startPos && canvasRef.current) {
      const rect = canvasRef.current.getBoundingClientRect();
      const currentX = e.clientX - rect.left;
      const currentY = e.clientY - rect.top;
      const w = currentX - startPos.x;
      const h = currentY - startPos.y;

      broadcastEvent('DRAW_SHAPE', {
        shape: tool,
        x: startPos.x,
        y: startPos.y,
        w,
        h,
        color: strokeColor,
        width: strokeWidth,
      });
    }
    setStartPos(null);
  };

  const handleClear = () => {
    if (!canvasRef.current || !ctxRef.current) return;
    ctxRef.current.fillStyle = '#0f172a';
    ctxRef.current.fillRect(0, 0, canvasRef.current.width, canvasRef.current.height);
    broadcastEvent('CLEAR_CANVAS', {});
  };

  const handleSaveSnapshot = async () => {
    if (!canvasRef.current || !currentSession) return;
    const dataUrl = canvasRef.current.toDataURL('image/png');
    try {
      await api.post(`/whiteboard/sessions/${currentSession.sessionCode}/snapshots`, {
        stateJson: dataUrl,
      });
      alert('Whiteboard state snapshot saved successfully!');
    } catch (err) {
      alert('Failed to save snapshot.');
    }
  };

  const handleExportPng = () => {
    if (!canvasRef.current) return;
    const link = document.createElement('a');
    link.download = `StudyHub-Whiteboard-${currentSession?.sessionCode || 'export'}.png`;
    link.href = canvasRef.current.toDataURL('image/png');
    link.click();
  };

  // Create / Join Room
  const handleCreateSession = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    setIsCreating(true);
    setErrorMsg('');
    try {
      const res = await api.post('/whiteboard/sessions', { title: newTitle.trim() });
      setCurrentSession(res.data.data);
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create whiteboard room');
    } finally {
      setIsCreating(false);
    }
  };

  const handleJoinSession = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sessionCodeInput.trim()) return;
    setIsJoining(true);
    setErrorMsg('');
    try {
      const res = await api.get(`/whiteboard/sessions/${sessionCodeInput.trim().toUpperCase()}`);
      if (!res.data.data.active) {
        setErrorMsg('This study room session is no longer active.');
        return;
      }
      setCurrentSession(res.data.data);
    } catch (err: any) {
      setErrorMsg('Session not found. Please check room code.');
    } finally {
      setIsJoining(false);
    }
  };

  const handleEndSession = async () => {
    if (!currentSession) return;
    if (window.confirm('Are you sure you want to end this room for all participants?')) {
      try {
        await api.post(`/whiteboard/sessions/${currentSession.sessionCode}/end`);
        setCurrentSession(null);
      } catch (err) {
        alert('Failed to end session');
      }
    }
  };

  if (!isAuthenticated) {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center' }} className="glass-panel">
        <div style={{ padding: '3rem 2rem' }}>
          <Lock size={48} color="var(--accent-primary)" style={{ margin: '0 auto 1rem' }} />
          <h2>Collaborative Study Room</h2>
          <p style={{ color: 'var(--text-secondary)', margin: '1rem 0 1.5rem' }}>
            Sign in to start or join real-time interactive whiteboard sessions with peers.
          </p>
          <a href="/login" className="btn btn-primary">Sign In</a>
        </div>
      </div>
    );
  }

  // Room Lobby View (Create or Join)
  if (!currentSession) {
    return (
      <div style={{ maxWidth: '800px', margin: '3rem auto', padding: '0 1.5rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '2.5rem' }}>
          <h1 style={{ fontSize: '2.2rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.6rem' }}>
            <PenTool size={32} color="var(--accent-cyan)" /> Live Study Whiteboard
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Real-time multi-peer drawing, mathematical derivations, and collaborative problem solving.
          </p>
        </div>

        {errorMsg && (
          <div style={{ background: 'rgba(244,63,94,0.15)', color: '#fda4af', padding: '0.75rem 1rem', borderRadius: 'var(--radius-md)', marginBottom: '1.5rem', textAlign: 'center' }}>
            {errorMsg}
          </div>
        )}

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
          
          {/* Create Room Card */}
          <div className="glass-panel" style={{ padding: '2rem' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Host a Study Room</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Create a unique room code and invite friends to collaborate synchronously.
            </p>
            <form onSubmit={handleCreateSession}>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: '0.4rem' }}>Room Title</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. Graph Algorithms Revision" 
                  value={newTitle} 
                  onChange={(e) => setNewTitle(e.target.value)}
                  className="input-field"
                />
              </div>
              <button type="submit" disabled={isCreating} className="btn btn-primary" style={{ width: '100%' }}>
                {isCreating ? <RefreshCw className="animate-spin" size={16} /> : <Plus size={16} />}
                {isCreating ? 'Creating Room...' : 'Start New Session'}
              </button>
            </form>
          </div>

          {/* Join Room Card */}
          <div className="glass-panel" style={{ padding: '2rem' }}>
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Join Existing Room</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Enter the 8-character room code shared by your peer host.
            </p>
            <form onSubmit={handleJoinSession}>
              <div style={{ marginBottom: '1.25rem' }}>
                <label style={{ fontSize: '0.85rem', fontWeight: 600, display: 'block', marginBottom: '0.4rem' }}>Session Code</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. WB-AB12CD34" 
                  value={sessionCodeInput} 
                  onChange={(e) => setSessionCodeInput(e.target.value.toUpperCase())}
                  className="input-field"
                  style={{ textTransform: 'uppercase', letterSpacing: '0.05em' }}
                />
              </div>
              <button type="submit" disabled={isJoining} className="btn btn-secondary" style={{ width: '100%' }}>
                {isJoining ? <RefreshCw className="animate-spin" size={16} /> : <Users size={16} />}
                {isJoining ? 'Connecting...' : 'Join Room'}
              </button>
            </form>
          </div>

        </div>
      </div>
    );
  }

  // Active Whiteboard Canvas View
  const isHost = user?.id === currentSession.hostId;

  return (
    <div style={{ maxWidth: '1380px', margin: '0 auto', padding: '1rem 1.5rem' }}>
      
      {/* Top Session Bar */}
      <div className="glass-panel" style={{ padding: '0.75rem 1.25rem', marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div>
            <h2 style={{ fontSize: '1.15rem', color: '#ffffff' }}>{currentSession.title}</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
              <span>Host: <strong style={{ color: '#ffffff' }}>{currentSession.hostName}</strong></span>
              <span>•</span>
              <span>Room Code: <strong style={{ color: 'var(--accent-cyan)', letterSpacing: '0.05em' }}>{currentSession.sessionCode}</strong></span>
            </div>
          </div>
          <button 
            onClick={() => navigator.clipboard.writeText(currentSession.sessionCode)}
            className="btn btn-secondary btn-sm"
            style={{ fontSize: '0.75rem' }}
            title="Copy Code"
          >
            <Share2 size={12} /> Copy Code
          </button>
        </div>

        {/* Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <button onClick={handleSaveSnapshot} className="btn btn-secondary btn-sm">
            <Save size={14} /> Save Snapshot
          </button>
          <button onClick={handleExportPng} className="btn btn-secondary btn-sm">
            <Download size={14} /> Export PNG
          </button>
          {isHost ? (
            <button onClick={handleEndSession} className="btn btn-danger btn-sm">
              <LogOut size={14} /> End Room
            </button>
          ) : (
            <button onClick={() => setCurrentSession(null)} className="btn btn-secondary btn-sm">
              <LogOut size={14} /> Leave Room
            </button>
          )}
        </div>
      </div>

      {/* Main Canvas + Tool Toolbar */}
      <div style={{ display: 'flex', gap: '1rem' }}>
        
        {/* Left Toolbar */}
        <div className="glass-panel" style={{ padding: '1rem 0.75rem', display: 'flex', flexDirection: 'column', gap: '0.75rem', alignItems: 'center', width: '60px' }}>
          <button 
            onClick={() => setTool('pen')} 
            className={`btn btn-sm ${tool === 'pen' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Pencil"
          >
            <PenTool size={16} />
          </button>

          <button 
            onClick={() => setTool('rectangle')} 
            className={`btn btn-sm ${tool === 'rectangle' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Rectangle"
          >
            <Square size={16} />
          </button>

          <button 
            onClick={() => setTool('circle')} 
            className={`btn btn-sm ${tool === 'circle' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Circle"
          >
            <Circle size={16} />
          </button>

          <button 
            onClick={() => setTool('line')} 
            className={`btn btn-sm ${tool === 'line' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Line"
          >
            <Minus size={16} />
          </button>

          <button 
            onClick={() => setTool('eraser')} 
            className={`btn btn-sm ${tool === 'eraser' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Eraser"
          >
            <Eraser size={16} />
          </button>

          <div style={{ width: '100%', height: '1px', background: 'var(--border-subtle)', margin: '0.25rem 0' }} />

          {/* Color Palettes */}
          {['#ffffff', '#6366f1', '#06b6d4', '#10b981', '#f59e0b', '#f43f5e'].map((c) => (
            <button
              key={c}
              onClick={() => { setStrokeColor(c); if (tool === 'eraser') setTool('pen'); }}
              style={{
                width: '22px',
                height: '22px',
                borderRadius: '50%',
                background: c,
                border: strokeColor === c ? '2px solid #ffffff' : '1px solid rgba(255,255,255,0.2)',
                cursor: 'pointer',
              }}
            />
          ))}

          <div style={{ width: '100%', height: '1px', background: 'var(--border-subtle)', margin: '0.25rem 0' }} />

          <button 
            onClick={handleClear} 
            className="btn btn-danger btn-sm"
            style={{ padding: '0.5rem', borderRadius: 'var(--radius-md)' }}
            title="Clear Canvas"
          >
            <Trash2 size={16} />
          </button>
        </div>

        {/* Canvas Area */}
        <div className="glass-panel" style={{ flex: 1, overflow: 'hidden', padding: 0, position: 'relative' }}>
          <canvas
            ref={canvasRef}
            onMouseDown={startDrawing}
            onMouseMove={draw}
            onMouseUp={stopDrawing}
            onMouseLeave={stopDrawing}
            style={{ display: 'block', width: '100%', cursor: tool === 'eraser' ? 'crosshair' : 'crosshair' }}
          />
        </div>

      </div>
    </div>
  );
};
