import styles from './ListPageLayout.module.css';

// Generic two-column layout for list pages:
//   <ListPageLayout
//      sidebar={<FilterPanel/>}
//      toolbar={<Toolbar.../>}
//   >
//     ...cards or skeleton or empty state...
//   </ListPageLayout>
export default function ListPageLayout({ sidebar, toolbar, children, sidebarTitle = '필터' }) {
  return (
    <div className={styles.wrap}>
      <aside className={styles.sidebar} aria-label={sidebarTitle}>
        <div className={styles.sidebarInner}>{sidebar}</div>
      </aside>
      <section className={styles.main}>
        {toolbar && <div className={styles.toolbar}>{toolbar}</div>}
        <div className={styles.content}>{children}</div>
      </section>
    </div>
  );
}

export function ResultsGrid({ children, columns }) {
  return (
    <div
      className={styles.grid}
      style={columns ? { gridTemplateColumns: columns } : undefined}
    >{children}</div>
  );
}
