"use client";

import React, { useState, useRef, useEffect } from 'react';
import styles from '../page.module.css';
import { Briefcase, Bot, X, Send, User, ArrowLeft, Target } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import Link from 'next/link';
import { UserButton } from "@clerk/nextjs";

const CONFIG = {
  title: 'Interview Prep',
  desc: 'Practice mock interviews and get tips for behavioral and technical questions.',
  icon: Target,
  greeting: 'Ready to ace your interview? Tell me the role and company you are interviewing for, and we can start a mock interview!',
  systemContext: 'You are an Interview Coach. Conduct mock interviews. Ask one question at a time. Provide feedback on the user\'s answers.'
};

export default function InterviewPrepPage() {
  const [messages, setMessages] = useState([
    { role: 'ai', content: CONFIG.greeting }
  ]);
  const [input, setInput] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() && !file) return;

    const userMessage = input.trim();
    setMessages(prev => [...prev, { role: 'user', content: userMessage + (file ? `\n(Attached File: ${file.name})` : '') }]);
    setInput('');
    setIsTyping(true);

    try {
      const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8000';
      const formData = new FormData();
      formData.append('message', userMessage);
      formData.append('modelChoice', 'gemini');
      formData.append('systemContext', CONFIG.systemContext);
      if (file) formData.append('file', file);
      
      setFile(null);
      const res = await fetch(`${API_URL}/api/chat`, { method: 'POST', body: formData });
      if (!res.ok) throw new Error('Failed to get response');
      const data = await res.json();
      setMessages(prev => [...prev, { role: 'ai', content: data.reply }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'ai', content: "Sorry, I couldn't connect to the backend server." }]);
    } finally {
      setIsTyping(false);
    }
  };

  const IconComponent = CONFIG.icon;

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <Briefcase className="gradient-text" size={32} />
          <span className="gradient-text">HireMate</span>
        </div>
        <nav style={{display:'flex', gap:'1.5rem', marginLeft:'3rem', marginRight:'auto'}}>
          <Link href="/" style={{color: 'var(--text-muted)', textDecoration: 'none', display:'flex', alignItems:'center', gap:'0.5rem'}}>
            <ArrowLeft size={16}/> Back to Dashboard
          </Link>
        </nav>
        <UserButton />
      </header>

      <main className={styles.main} style={{gridTemplateColumns: '1fr', maxWidth: '1000px', margin: '0 auto', width: '100%'}}>
        <section className={`${styles.chatArea} glass-panel`} style={{height: '100%'}}>
          <div className={styles.chatHeader} style={{borderBottomColor: 'rgba(139, 92, 246, 0.3)', display: 'flex', flexDirection: 'column'}}>
            <h2 style={{display:'flex', alignItems:'center', gap:'0.5rem'}}>
              <IconComponent size={24} color="var(--primary)"/> {CONFIG.title}
            </h2>
            <p style={{marginTop: '0.25rem'}}>{CONFIG.desc}</p>
          </div>

          <div className={styles.messages}>
            {messages.map((msg, idx) => (
              <div key={idx} className={`${styles.message} ${msg.role === 'user' ? styles.messageUser : styles.messageAI}`}>
                <div className={styles.messageRole}>
                  {msg.role === 'user' ? (
                    <span style={{display: 'flex', alignItems: 'center', gap: '0.5rem'}}><User size={14}/> You</span>
                  ) : (
                    <span style={{display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)'}}><Bot size={14}/> {CONFIG.title} Agent</span>
                  )}
                </div>
                <div className={styles.messageContent}>
                  {msg.role === 'ai' ? <ReactMarkdown remarkPlugins={[remarkGfm]}>{msg.content}</ReactMarkdown> : msg.content}
                </div>
              </div>
            ))}
            {isTyping && <div className={styles.typingIndicator}>Agent is thinking...</div>}
            <div ref={messagesEndRef} />
          </div>

          <div className={styles.inputArea}>
            {file && (
              <div className={styles.fileIndicator}>
                <Briefcase size={16} />
                <span>{file.name}</span>
                <button type="button" onClick={() => setFile(null)} className={styles.removeFileBtn}><X size={16} /></button>
              </div>
            )}
            <form onSubmit={handleSubmit} className={styles.inputForm}>
              <label className={styles.uploadButton} title="Attach PDF Resume">
                <input type="file" accept=".pdf" style={{ display: 'none' }} onChange={(e) => setFile(e.target.files ? e.target.files[0] : null)} />
                <Bot size={20} />
              </label>
              <input type="text" className={styles.input} placeholder={`Ask for ${CONFIG.title}...`} value={input} onChange={(e) => setInput(e.target.value)} />
              <button type="submit" className={styles.sendButton} disabled={(!input.trim() && !file) || isTyping}><Send size={20} /></button>
            </form>
          </div>
        </section>
      </main>
    </div>
  );
}
