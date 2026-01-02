import {toast} from "react-toastify";
import {getErrorMessage} from "./getErrorMessage.js";

export const withToastError = async (fn, fallbackMessage) => {
    try {
        return await fn();
    } catch (err) {
        toast.error(getErrorMessage(err, fallbackMessage));
        throw err;
    }
};
