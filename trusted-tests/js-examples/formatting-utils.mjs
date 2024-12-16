import moment from "moment";

function FormattingUtils(moment) {
  const dateTimeFormatter = new Intl.DateTimeFormat();

  return {
    /**
     * @param {Iso8601DateTime} datetime
     * @returns {number}
     */
    daysSince: function (datetime) {
      if (!datetime) return "";
      return moment().diff(moment(datetime), "days") + "";
    },

    /**
     * @param {Iso8601DateTime} datetime
     * @returns {string}
     */
    formatFullDate: function (datetime) {
      return dateTimeFormatter.format(new Date(datetime));
    },
  };
}

export default FormattingUtils(moment);
