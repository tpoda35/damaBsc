export const getErrorMessage = (err, fallbackMessage = 'An error occurred') => {
    return err?.response?.data?.message || err?.message || fallbackMessage;
};
