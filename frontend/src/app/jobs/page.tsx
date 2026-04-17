"use client";

import React, { useState, useRef, useEffect } from 'react';
import styles from '../page.module.css';
import { Briefcase, Bot, Paperclip, X, Search, User } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import Link from 'next/link';
import { UserButton } from "@clerk/nextjs";

export default function JobsPage() {
  const [messages, setMessages] = useState([
    { role: 'ai', content: 'Upload your resume and tell me your target roles or location. I will search the web for the latest open job postings and provide links to apply!' }
  ]);
  const [input, setInput] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

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
    setMessages(prev => [...prev, { role: 'user', content: userMessage + (file ? `\n(Attached Resume: ${file.name})` : '') }]);
    setInput('');
    setIsTyping(true);

    try {
      const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://127.0.0.1:8000';
      
      const formData = new FormData();
      // Pre-fill the prompt so the AI acts as a search agent
      formData.append('message', "I am looking for jobs online. My preferences/details are: " + userMessage + ". Please search the web for active job listings that match my resume skills and these preferences. Give me a list of job titles, companies, locations, and direct links to apply.");
      formData.append('modelChoice', 'gemini');
      formData.append('enableSearch', 'true');
      if (file) {
        formData.append('file', file);
      }
      
      setFile(null);

      const res = await fetch(`${API_URL}/api/chat`, {
        method: 'POST',
        body: formData
      });
      
      if (!res.ok) {
        throw new Error('Failed to get response');
      }
      
      const data = await res.json();
      setMessages(prev => [...prev, { role: 'ai', content: data.reply }]);
    } catch (error) {
      console.error(error);
      setMessages(prev => [...prev, { role: 'ai', content: "Sorry, I couldn't connect to the backend server to perform the search." }]);
    } finally {
      setIsTyping(false);
    }
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <Briefcase className="gradient-text" size={32} />
          <span className="gradient-text">HireMate</span>
        </div>
        <nav style={{display:'flex', gap:'1.5rem', marginLeft:'3rem', marginRight:'auto', fontSize:'1rem'}}>
          <Link href="/" style={{color: 'var(--text-muted)', textDecoration: 'none'}}>Assistant</Link>
          <Link href="/jobs" style={{color: 'var(--primary)', fontWeight: 'bold', textDecoration: 'none'}}>Web Job Search</Link>
        </nav>
        <UserButton />
      </header>

      <main className={styles.main} style={{gridTemplateColumns: '1fr', maxWidth: '900px', margin: '0 auto', width: '100%'}}>
        <section className={`${styles.chatArea} glass-panel`} style={{height: '100%'}}>
          <div className={styles.chatHeader} style={{borderBottomColor: 'rgba(139, 92, 246, 0.3)'}}>
            <h2 style={{display:'flex', alignItems:'center', gap:'0.5rem'}}><Search size={24} color="var(--primary)"/> Live Web Job Search</h2>
            <p>Upload a resume and let our AI scour the internet for your perfect role.</p>
          </div>

          <div className={styles.messages}>
            {messages.map((msg, idx) => (
              <div 
                key={idx} 
                className={`${styles.message} ${msg.role === 'user' ? styles.messageUser : styles.messageAI}`}
              >
                <div className={styles.messageRole}>
                  {msg.role === 'user' ? (
                    <span style={{display: 'flex', alignItems: 'center', gap: '0.5rem'}}>
                      <User size={14}/> You
                    </span>
                  ) : (
                    <span style={{display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)'}}>
                      <Bot size={14}/> HireMate Agent
                    </span>
                  )}
                </div>
                <div className={styles.messageContent}>
                  {msg.role === 'ai' ? (
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {msg.content}
                    </ReactMarkdown>
                  ) : (
                    msg.content
                  )}
                </div>
              </div>
            ))}
            {isTyping && (
              <div className={styles.typingIndicator}>
                Scouring the web for active job listings...
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <div className={styles.inputArea}>
            {file && (
              <div className={styles.fileIndicator}>
                <Briefcase size={16} />
                <span>{file.name}</span>
                <button type="button" onClick={() => setFile(null)} className={styles.removeFileBtn}>
                  <X size={16} />
                </button>
              </div>
            )}
            <form onSubmit={handleSubmit} className={styles.inputForm}>
              <label className={styles.uploadButton} title="Attach Resume PDF">
                <input 
                  type="file" 
                  accept=".pdf"
                  style={{ display: 'none' }}
                  onChange={(e) => setFile(e.target.files ? e.target.files[0] : null)}
                />
                <Paperclip size={20} />
              </label>
              <input 
                type="text" 
                ref={inputRef}
                className={styles.input} 
                placeholder="e.g. Find me remote frontend roles..." 
                value={input}
                onChange={(e) => setInput(e.target.value)}
              />
              <button 
                type="submit" 
                className={styles.sendButton}
                disabled={(!input.trim() && !file) || isTyping}
              >
                <Search size={20} />
              </button>
            </form>
          </div>
        </section>
      </main>
    </div>
  );
}
