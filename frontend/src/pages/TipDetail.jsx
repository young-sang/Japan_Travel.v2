import { useParams, Navigate } from 'react-router-dom';
import { TIP_DETAILS } from '../data/tipDetails.js';
import styles from './TipDetail.module.css';

export default function TipDetail() {
  const { slug } = useParams();
  const data = TIP_DETAILS[slug];
  if (!data) return <Navigate to="/tip" replace />;

  return (
    <main className={styles.container} style={{ maxWidth: data.maxWidth }}>
      <section
        className={styles.tipHero}
        style={{ background: data.heroBg, color: data.heroColor }}
      >
        <h1>{data.title}</h1>
        <p>{data.heroDesc}</p>
      </section>

      <section className={styles.tipContent}>
        {data.cards.map((c, i) => (
          <div key={i} className={styles.card}>
            <h3 style={{ color: data.accent }}>{c.title}</h3>
            <p>{c.body}</p>
          </div>
        ))}
      </section>
    </main>
  );
}
