import { useEffect, useState } from 'react';
import { api } from '../api/client.js';

export default function FxWidget() {
  const [v, setV] = useState(null);
  useEffect(() => {
    api.fx().then((r) => setV(r.value)).catch(() => {});
  }, []);
  if (v == null) return null;
  return <span title="100엔당 원화" style={{ fontSize: '13px', color: '#555' }}>💴 100¥ = {v.toFixed(0)}원</span>;
}
