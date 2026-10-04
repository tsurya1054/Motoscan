export interface PID {
  id: string;
  name: string;
  shortName: string;
  obdPid: string;
  value: number;
  unit: string;
  category: 'ENGINE' | 'SENSOR' | 'OTHER';
  min: number;
  max: number;
  warnMin?: number;
  warnMax?: number;
  colorOptimal?: string;
  colorLow?: string;
  colorHigh?: string;
}

export interface DTC {
  code: string;
  description: string;
  status: 'ERROR' | 'PENDING';
  category: string;
}

export interface ReadinessItem {
  id: string;
  name: string;
  nameIndo: string;
  status: 'READY' | 'NOT_READY';
}

export interface LogFile {
  name: string;
  date: string;
  size: string;
  isFavorite: boolean;
  content: string;
}

export interface MotorcycleSpecs {
  engineDisplacementCc: number; // e.g., 150 cc, 180 cc
  cylinderCount: number; // e.g., 1 silinder
  engineType: string; // e.g., 4-Langkah, SOHC, eSP
  boreStroke: string; // e.g., 57.3 x 57.9 mm
  compressionRatio: string; // e.g., 10.6 : 1
  fuelTankCapacity: number; // e.g., 8.0 Liter
  transmissionType: string; // e.g., Otomatis, V-Matic (CVT)
  modelYear: number; // e.g., 2022
}

export interface OBD2Settings {
  connectionType: 'Bluetooth' | 'WiFi' | 'Simulation';
  pairedDevice: string;
  autoConnect: boolean;
  updateRate: number; // Hz
  units: 'Metric' | 'Imperial';
  language: 'English' | 'Indonesian';
  profileName: string;
  // Spek Lengkap Motor
  specs: MotorcycleSpecs;
}
