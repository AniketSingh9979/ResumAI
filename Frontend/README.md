# Interview Test Platform - Frontend

A comprehensive Angular application for conducting technical interviews with coding questions, multiple choice questions, and an AI-powered Q&A chat system.

## Features

### 🎯 Complete Interview Solution
- **Left Sidebar Navigation**: Switch between Coding, MCQ, and Q&A sections
- **Top Bar**: Real-time countdown timer (45 minutes) and candidate information display
- **Bottom Navigation**: Previous/Next navigation and Submit functionality

### 💻 Coding Section
- Interactive coding questions with descriptions
- Built-in code editor (textarea-based)
- Question navigation with progress tracking
- Auto-save functionality for answers

### ❓ Multiple Choice Questions (MCQ)
- Support for both single and multiple choice questions
- Radio buttons for single choice, checkboxes for multiple choice
- Comprehensive question set covering technical topics
- Real-time answer tracking

### 🤖 AI Chat Bot (Q&A Section)
- Interactive chat interface with AI assistant
- Real-time messaging with typing indicators
- Chat history tracking for submission
- Professional chat UI with timestamps

### ⏰ Timer & Candidate Management
- 45-minute countdown timer with visual warnings
- Candidate information display (name, role, experience)
- Automatic submission when time expires

### 📊 Data Management
- Comprehensive answer collection from all sections
- RESTful API integration ready
- Interview submission with complete data bundle
- Dummy data for development and testing

## Project Structure

\`\`\`
Frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── interview-test-page/          # Main interview component
│   │   │   │   ├── interview-test-page.component.ts
│   │   │   │   ├── interview-test-page.component.html
│   │   │   │   └── interview-test-page.component.css
│   │   │   └── ai-chat-bot/                 # Chat component
│   │   │       ├── ai-chat-bot.component.ts
│   │   │       ├── ai-chat-bot.component.html
│   │   │       └── ai-chat-bot.component.css
│   │   ├── services/
│   │   │   └── interview.service.ts         # API service
│   │   └── app.component.ts                 # Root component
│   ├── styles.css                           # Global styles
│   ├── index.html
│   └── main.ts
├── package.json
├── angular.json
├── tsconfig.json
└── README.md
\`\`\`

## Installation & Setup

### Prerequisites
- Node.js (version 18 or higher)
- npm or yarn package manager
- Angular CLI (optional, for development)

### Installation Steps

1. **Navigate to the Frontend directory**:
   \`\`\`bash
   cd Frontend
   \`\`\`

2. **Install dependencies**:
   \`\`\`bash
   npm install
   \`\`\`

3. **Start the development server**:
   \`\`\`bash
   npm start
   \`\`\`

4. **Open your browser**:
   Navigate to \`http://localhost:4200\`

## Usage

### For Interviewers
1. **Setup**: The application starts with default candidate information
2. **Timer**: 45-minute countdown begins automatically
3. **Sections**: Use the left sidebar to navigate between:
   - **Coding**: Present coding challenges
   - **MCQ**: Technical multiple choice questions  
   - **Q&A**: AI-assisted question/answer session

### For Candidates
1. **Navigation**: Use sidebar to switch between test sections
2. **Coding Section**: 
   - Read question descriptions carefully
   - Write code solutions in the provided editor
   - Use Previous/Next to navigate between coding questions
3. **MCQ Section**:
   - Answer all multiple choice questions
   - Select single or multiple options as indicated
4. **Q&A Section**:
   - Chat with the AI assistant
   - Ask technical questions or seek clarifications
5. **Submission**: Click "Submit Interview" when complete

## API Integration

### InterviewService
The \`InterviewService\` handles all data operations:

\`\`\`typescript
// Get coding questions
getCodingQuestions(): Observable<CodingQuestion[]>

// Get MCQ questions  
getMCQQuestions(): Observable<MCQQuestion[]>

// Submit complete interview
submitInterview(submission: InterviewSubmission): Observable<any>
\`\`\`

### Data Models
- **CodingQuestion**: Question metadata and starter code
- **MCQQuestion**: Question text, options, and answer type
- **InterviewSubmission**: Complete interview data bundle
- **ChatMessage**: Q&A chat history

### Backend API Endpoints (Ready for Integration)
- \`GET /api/interview/coding-questions\` - Fetch coding questions
- \`GET /api/interview/mcq-questions\` - Fetch MCQ questions
- \`POST /api/interview/submit\` - Submit complete interview

## Development

### Component Architecture
- **Standalone Components**: Using Angular 17+ standalone component architecture
- **Reactive Forms**: Form handling with Angular Reactive Forms
- **RxJS**: Reactive programming for data management
- **TypeScript**: Full type safety throughout the application

### Key Features
- **Responsive Design**: Mobile-friendly interface
- **Real-time Updates**: Live timer and chat functionality
- **Data Persistence**: Answer auto-saving during navigation
- **Professional UI**: Modern, clean design with smooth animations

### Customization
- **Questions**: Modify dummy data in \`InterviewService\`
- **Timer**: Adjust duration in \`InterviewTestPageComponent\`
- **Styling**: Update CSS files for custom branding
- **AI Responses**: Customize chatbot responses in \`AiChatBotComponent\`

## Production Deployment

### Build for Production
\`\`\`bash
npm run build
\`\`\`

### Environment Configuration
Update API endpoints in \`InterviewService\` for production:
\`\`\`typescript
private apiUrl = 'https://your-api-domain.com/api/interview';
\`\`\`

## Browser Support
- Chrome (recommended)
- Firefox
- Safari
- Edge

## License
This project is part of an interview system solution.

## Support
For questions or issues, please refer to the project documentation or contact the development team. 