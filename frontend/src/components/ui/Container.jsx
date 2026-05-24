import styles from './Container.module.css';

export default function Container({ size = 'default', children, className = '', as: Tag = 'div', ...rest }) {
  const sizeClass = size === 'narrow' ? styles.narrow : size === 'wide' ? styles.wide : styles.base;
  return (
    <Tag className={`${styles.container} ${sizeClass} ${className}`} {...rest}>
      {children}
    </Tag>
  );
}
