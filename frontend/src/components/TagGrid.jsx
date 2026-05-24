import Tag from './ui/Tag.jsx';
import styles from './FilterGroup.module.css';

export default function TagGrid({ tags, value, onChange, title = '테마', prefix = '#' }) {
  return (
    <div className={styles.group}>
      <h3 className={styles.title}>{title}</h3>
      <div className={styles.tags}>
        {tags.map((t) => (
          <Tag
            key={t}
            size="sm"
            selected={t === value}
            onClick={() => onChange(t === value ? '' : t)}
          >{prefix}{t}</Tag>
        ))}
      </div>
    </div>
  );
}
