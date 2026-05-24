import { useEffect, useState } from 'react';
import { api } from '../api/client.js';

const ICON = { 0:'☀️', 1:'🌤', 2:'⛅', 3:'☁️', 45:'🌫', 48:'🌫', 51:'🌦', 53:'🌦', 55:'🌧', 61:'🌧', 63:'🌧', 65:'🌧', 71:'❄️', 73:'❄️', 75:'❄️', 80:'🌦', 81:'🌧', 82:'⛈', 95:'⛈', 96:'⛈', 99:'⛈' };

export default function WeatherWidget() {
  const [data, setData] = useState(null);
  useEffect(() => {
    api.weather('도쿄도').then(setData).catch(() => {});
  }, []);
  if (!data || data.error) return null;
  const icon = ICON[data.weatherCode] || '🌡';
  return <span title={`도쿄 ${data.temperature}℃`} style={{ fontSize: '13px', color: '#555' }}>{icon} {Math.round(data.temperature)}℃</span>;
}
