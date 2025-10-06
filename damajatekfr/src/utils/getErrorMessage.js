export const getErrorMessage = (err, fallbackMessage = 'An error occurred') => {
    if (err?.response?.data) {
        const data = err.response.data;
        return data.message || data.error || fallbackMessage;
    }
    return err?.message || fallbackMessage;
};
