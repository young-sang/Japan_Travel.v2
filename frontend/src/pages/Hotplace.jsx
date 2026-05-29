import styles from './Hotplace.module.css';

const SPOTS = [
  {
    images: ['/image/hotplace1_1.png', '/image/hotplace1_2.png'],
    alt: '미야코지마시',
    title: '미야코지마시 (오키나와현): 에메랄드빛 바다의 낙원',
    desc: '투명한 바다와 새하얀 해변이 펼쳐지는 미야코지마시는 일본 속 남국 휴양지의 매력을 온전히 느낄 수 있는 곳입니다. 요나하 마에하마 해변에서 여유로운 시간을 보내고, 이라부대교를 따라 드라이브하며 끝없이 이어지는 푸른 풍경을 감상해보세요. 밤에는 잔잔한 파도 소리와 함께 오키나와 특유의 느긋한 분위기가 여행의 피로를 편안하게 녹여줍니다.',
  },
  {
    images: ['/image/hotplace2_1.png', '/image/hotplace2_2.png'],
    alt: '마쓰야마',
    title: '마쓰야마 (에히메현): 온천과 역사 감성의 도시',
    desc: '일본에서 가장 오래된 온천으로 알려진 도고 온천은 마쓰야마 여행의 상징과도 같은 장소입니다. 따뜻한 온천수에 몸을 담근 뒤, 고즈넉한 분위기의 상점가를 걸으며 일본 전통 감성을 느껴보세요. 언덕 위 마쓰야마성에서는 도시와 세토내해를 한눈에 바라볼 수 있으며, 여유롭고 차분한 분위기가 힐링 여행에 특별한 매력을 더해줍니다.',
  },
  {
    images: ['/image/hotplace3_1.png', '/image/hotplace3_2.png'],
    alt: '하코다테',
    title: '하코다테 (홋카이도): 낭만적인 야경과 항구 도시의 매력',
    desc: '하코다테는 일본 3대 야경 중 하나로 손꼽히는 아름다운 전망과 신선한 해산물로 유명한 홋카이도의 대표 관광 도시입니다. 하코다테산 전망대에서 반짝이는 도시 야경을 감상하고, 아침 시장에서는 갓 잡은 해산물 덮밥을 맛보세요. 붉은 벽돌 창고와 서양식 건축물이 어우러진 거리 풍경은 이국적인 분위기를 자아내며 특별한 여행의 추억을 선사합니다.',
  },
];

export default function Hotplace() {
  return (
    <div className={styles.page}>
      <div className={styles['fest-header']}>
        <h2>🔥 핫플레이스</h2>
        <p>일본 현지인들이 사랑하는 핫플레이스만 모았습니다.</p>
      </div>
      <div className={styles.container}>
        {SPOTS.map((spot, i) => (
          <div key={i} className={styles.card}>
            <div className={styles['image-row']}>
              {spot.images.map((src, j) => (
                <img key={j} src={src} alt={spot.alt} />
              ))}
            </div>
            <div className={styles.content}>
              <h2>{spot.title}</h2>
              <p>{spot.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
