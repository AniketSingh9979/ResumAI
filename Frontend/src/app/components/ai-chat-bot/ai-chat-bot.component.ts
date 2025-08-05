import { Component, EventEmitter, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';

export interface ChatMessage {
  id: string;
  message: string;
  sender: 'user' | 'ai';
  timestamp: Date;
}

@Component({
  selector: 'app-ai-chat-bot',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ai-chat-bot.component.html',
  styleUrls: ['./ai-chat-bot.component.css']
})
export class AiChatBotComponent implements OnInit {
  @Output() chatUpdate = new EventEmitter<ChatMessage[]>();
  
  chatForm: FormGroup;
  messages: ChatMessage[] = [];
  isTyping = false;

  // Sample AI responses for demo
  private aiResponses = [
    "I understand your question. Could you provide more details about the specific aspect you'd like me to explain?",
    "That's a great question! Based on my understanding, here's what I think...",
    "Let me break this down for you step by step:",
    "I see what you're asking about. This is actually a common topic in technical interviews.",
    "From a technical perspective, there are several approaches to consider...",
    "That's an interesting point. Have you considered the trade-offs between different solutions?",
    "Great question! This relates to some fundamental concepts in software development.",
    "I'd be happy to help clarify that. Let me explain the concept with an example.",
    "This is definitely worth discussing. What's your initial approach to solving this?",
    "Excellent question! This is something many developers encounter in real-world scenarios."
  ];

  constructor(private fb: FormBuilder) {
    this.chatForm = this.fb.group({
      message: ['']
    });
  }

  ngOnInit() {
    // Add welcome message
    this.addMessage('Welcome to the Q&A session! Feel free to ask me any technical questions you have during the interview.', 'ai');
  }

  sendMessage() {
    const messageText = this.chatForm.value.message?.trim();
    if (!messageText) return;

    // Add user message
    this.addMessage(messageText, 'user');
    
    // Clear input
    this.chatForm.patchValue({ message: '' });
    
    // Simulate AI typing
    this.isTyping = true;
    
    // Simulate AI response after delay
    setTimeout(() => {
      const aiResponse = this.getRandomAIResponse();
      this.addMessage(aiResponse, 'ai');
      this.isTyping = false;
    }, 1000 + Math.random() * 2000); // Random delay 1-3 seconds
  }

  private addMessage(message: string, sender: 'user' | 'ai') {
    const newMessage: ChatMessage = {
      id: Date.now().toString() + Math.random(),
      message,
      sender,
      timestamp: new Date()
    };
    
    this.messages.push(newMessage);
    this.chatUpdate.emit([...this.messages]);
    
    // Scroll to bottom after message is added
    setTimeout(() => this.scrollToBottom(), 100);
  }

  private getRandomAIResponse(): string {
    return this.aiResponses[Math.floor(Math.random() * this.aiResponses.length)];
  }

  private scrollToBottom() {
    const chatContainer = document.querySelector('.chat-messages');
    if (chatContainer) {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
} 