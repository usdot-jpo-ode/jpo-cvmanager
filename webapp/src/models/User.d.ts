type User = {
  email: string;
  first_name: string;
  last_name: string;
  role: UserRole;
  id: string;
  email_preference: EmailPreferences;
};

type EmailPreferences = {
  receiveAnnouncements: boolean;
  notificationFrequency: EmailFrequency;
  receiveCeaseBroadcastRecommendations: boolean;
  receiveCriticalErrorMessages: boolean;
  receiveNewUserRequests: boolean;
};

type UserRole = "ADMIN" | "USER";

type EmailFrequency = "ALWAYS" | "ONCE_PER_HOUR" | "ONCE_PER_DAY" | "ONCE_PER_WEEK" | "ONCE_PER_MONTH" | "NEVER";

type KeycloakRole = {
  id: string;
  name: string;
};
