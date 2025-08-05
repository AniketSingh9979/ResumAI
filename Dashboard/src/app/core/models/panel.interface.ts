export interface Panel {
  id: string;
  name: string;
  email: string;
  designation: string;
  expertise: string[];
  availability: {
    date: Date;
    slots: {
      startTime: string;
      endTime: string;
    }[];
  }[];
}
