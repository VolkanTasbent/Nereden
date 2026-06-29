import { create } from 'zustand';

interface AppState {
  isOnboarded: boolean;
  isAppReady: boolean;
  setOnboarded: (value: boolean) => void;
  setAppReady: (value: boolean) => void;
}

export const useAppStore = create<AppState>((set) => ({
  isOnboarded: false,
  isAppReady: false,
  setOnboarded: (isOnboarded) => set({ isOnboarded }),
  setAppReady: (isAppReady) => set({ isAppReady }),
}));
