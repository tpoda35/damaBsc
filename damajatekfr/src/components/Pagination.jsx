import Button from "./Button.jsx";
import styles from "./Pagination.module.css";

const Pagination = ({
                        pageNum,
                        totalPages,
                        onPageChange
                    }) => {
    if (totalPages <= 1) return null;

    return (
        <div className={styles.pagination}>
            <Button
                onClick={() => onPageChange((prev) => Math.max(prev - 1, 0))}
                disabled={pageNum === 0}
            >
                Prev
            </Button>

            <span className={styles.pageInfo}>
                Page {pageNum + 1} of {totalPages}
            </span>

            <Button
                onClick={() =>
                    onPageChange((prev) =>
                        Math.min(prev + 1, totalPages - 1)
                    )
                }
                disabled={pageNum + 1 >= totalPages}
            >
                Next
            </Button>
        </div>
    );
};

export default Pagination;
