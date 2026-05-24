import { useState, useEffect, useRef } from 'react';
import { api } from '../api/client.js';
import EmptyState from './ui/EmptyState.jsx';
import Button from './ui/Button.jsx';
import Spinner from './ui/Spinner.jsx';
import { useToast } from './ui/Toast.jsx';

export default function EmptyStateCollector({ type, prefecture, onComplete, message }) {
  const [running, setRunning] = useState(false);
  const [run, setRun] = useState(null);
  const pollRef = useRef(null);
  const toast = useToast();

  useEffect(() => () => clearInterval(pollRef.current), []);

  async function start() {
    setRunning(true);
    try {
      const { runId } = await api.runCollector(type, prefecture);
      pollRef.current = setInterval(async () => {
        try {
          const r = await api.collectorRun(runId);
          setRun(r);
          if (r.status === 'success' || r.status === 'partial' || r.status === 'failed') {
            clearInterval(pollRef.current);
            setRunning(false);
            if (r.status === 'failed') toast.error('수집에 실패했습니다');
            else toast.success(`수집 완료 · 신규 ${r.itemsAdded}건`);
            if (onComplete) onComplete(r);
          }
        } catch (e) {
          clearInterval(pollRef.current);
          setRunning(false);
          toast.error('수집 상태 확인 중 오류');
        }
      }, 1500);
    } catch (e) {
      setRunning(false);
      toast.error('수집 시작에 실패했습니다');
    }
  }

  const label = type === 'festival' ? '축제' : '관광지';

  return (
    <EmptyState
      icon="📦"
      title={message || `${prefecture} ${label} 데이터가 아직 없어요`}
      description={running
        ? `진행 중 · 신규 ${run?.itemsAdded ?? 0} · 갱신 ${run?.itemsUpdated ?? 0} · 실패 ${run?.itemsFailed ?? 0}`
        : '위키백과에서 정보를 가져와 데이터베이스에 저장합니다 (수십 초 소요).'}
      primaryAction={
        running
          ? <Spinner size={28} label="수집 중" />
          : <Button variant="primary" size="md" onClick={start}>🚀 지금 수집하기</Button>
      }
    />
  );
}
