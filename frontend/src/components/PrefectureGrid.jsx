import { PREFECTURES } from '../data/prefectures.js';
import Tag from './ui/Tag.jsx';
import styles from './FilterGroup.module.css';

export default function PrefectureGrid({ value, onChange, title = '도도부현' }) {
  return (
    <div className={styles.group}>
      <h3 className={styles.title}>{title}</h3>
      <div className={styles.tags}>
        {PREFECTURES.map((p) => (
          <Tag
            key={p}
            size="sm"
            selected={p === value}
            onClick={() => onChange(p === value ? '' : p)}
          >{p}</Tag>
        ))}
      </div>
    </div>
  );
}
