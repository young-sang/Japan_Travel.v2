import { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { TIP_DETAILS } from '../data/tipDetails.js';
import styles from './Tip.module.css';
import usePageTitle from '../utils/usePageTitle.js';

//  카드 위쪽은 사진 대신 아이콘 + 상세 화면 히어로와 같은 그라데이션 (D-073)
const TIPS = [
  { slug: 'visa', icon: '🛂', subject: '무비자 입국',
    text: '한국 여권 소지자는 일본에 최대 90일 동안 관광·단기 방문 목적에 한해 비자 없이 입국할 수 있습니다.' },
  { slug: 'carry', icon: '🧳', subject: '기내 반입 가능 물품',
    text: '항공사의 물품 반입에 대한 정보를 알아볼 수 있습니다.' },
  { slug: 'attitude', icon: '🙇', subject: '일본 예절',
    text: '한국과는 사뭇 다른 일본의 예절에 대해 알아봅시다.' },
  { slug: 'rent', icon: '🚗', subject: '렌트카',
    text: '국제운전면허증, 여권, 운전면허증 원본이 필요하며, 좌측통행 운전 방식에 유의해야 합니다.' },
  { slug: 'accomo', icon: '🏨', subject: '숙소',
    text: '심미적인 분위기가 흐르는 호텔과 전통 료칸 등 다양한 숙박 시설을 제공하여 편안하고 특별한 여행을 즐길 수 있습니다.' },
  { slug: 'peace', icon: '🛡️', subject: '치안',
    text: '치안이 매우 우수하여 편안하고 쾌적한 하루를 제공합니다.' },
];

export default function Tip() {
  usePageTitle('여행 팁');
  const cardRefs = useRef([]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries, obs) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add(styles.visible);
            obs.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.18 }
    );
    cardRefs.current.forEach((el) => el && observer.observe(el));
    return () => observer.disconnect();
  }, []);

  function handleMouseMove(e, el) {
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const x = ((e.clientX - rect.left) / rect.width - 0.5) * 18;
    const y = ((e.clientY - rect.top) / rect.height - 0.5) * -18;
    el.style.transform = `perspective(900px) rotateX(${y}deg) rotateY(${x}deg) translateZ(0)`;
  }

  function handleMouseLeave(el) {
    if (!el) return;
    el.style.transform = 'translateY(0)';
  }

  return (
    <div className={styles.page}>
      <section className={styles.section}>
        <div className={styles.sectionHeader}>
          <h1 className={styles.mainTitle}>여행 팁</h1>
          <p className={styles.subTitle}>일본 여행 전에 알아야 할 것이 무엇이 있을까요?</p>
        </div>

        <div className={styles.cardGrid}>
          {TIPS.map((tip, i) => (
            <Link
              key={tip.slug}
              to={`/tip/${tip.slug}`}
              ref={(el) => (cardRefs.current[i] = el)}
              className={styles.tipCard}
              onMouseMove={(e) => handleMouseMove(e, cardRefs.current[i])}
              onMouseLeave={() => handleMouseLeave(cardRefs.current[i])}
            >
              <div className={styles.cardImageBox} style={{ background: TIP_DETAILS[tip.slug].heroBg }}>
                <span className={styles.cardIcon} aria-hidden="true">{tip.icon}</span>
              </div>
              <div className={styles.cardInfo}>
                <h2 className={styles.cardSubject}>{tip.subject}</h2>
                <p className={styles.cardText}>{tip.text}</p>
              </div>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
