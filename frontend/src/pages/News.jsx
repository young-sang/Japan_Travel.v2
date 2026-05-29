import styles from './News.module.css';

const MAIN = {
  href: 'https://www.yna.co.kr/view/AKR20251227021800073',
  img: '/image/news1.png',
  alt: '메인 뉴스 이미지',
  title: '일본, 2027년 7월부터 출국세가 3배?',
  desc: '일본 정부는 2027년 7월부터, 해외 관광객들의 과잉 여행(오버투어리즘)에 대항하기 위해, 출국세의 3배 인상 카드를 꺼내들었습니다.',
};

const SUBS = [
  {
    href: 'https://www.yna.co.kr/view/AKR20260402152000542',
    img: '/image/news2.png',
    alt: '서브 뉴스 1',
    title: "일본 동북 지역 관광지 이용에 편리한 '도호쿠 조이 패스' 출시",
  },
  {
    href: 'https://www.yna.co.kr/view/AKR20260409053700030',
    img: '/image/news3.png',
    alt: '서브 뉴스 2',
    title: '일본의 벚꽃 축제가 곧 시작합니다!',
  },
  {
    href: 'https://www.hani.co.kr/arti/international/international_general/1240664',
    img: '/image/news4.png',
    alt: '서브 뉴스 3',
    title: '일본 관광 4천만 시대 개막... 방일 외국인 소비액 9.5조엔 역대 최고',
  },
  {
    href: 'https://www.donga.com/news/Culture/article/all/20250515/131610147/1',
    img: '/image/news5.png',
    alt: '서브 뉴스 4',
    title: "식지 않는 '일본여행' 인기... Z세대 픽은 \"소도시\"",
  },
];

export default function News() {
  return (
    <div className={styles.container}>
      <a href={MAIN.href} className={`${styles.card} ${styles.mainCard}`} target="_blank" rel="noopener noreferrer">
        <div className={styles.cardImage}>
          <img src={MAIN.img} alt={MAIN.alt} />
        </div>
        <h2 className={styles.cardTitle}>{MAIN.title}</h2>
        <p className={styles.cardDesc}>{MAIN.desc}</p>
      </a>

      <div className={styles.subGrid}>
        {SUBS.map((s, i) => (
          <a key={i} href={s.href} className={`${styles.card} ${styles.subCard}`} target="_blank" rel="noopener noreferrer">
            <div className={styles.cardImage}>
              <img src={s.img} alt={s.alt} />
            </div>
            <h3 className={styles.cardTitle}>{s.title}</h3>
          </a>
        ))}
      </div>
    </div>
  );
}
