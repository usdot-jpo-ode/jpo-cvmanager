import PropTypes from "prop-types";
import { Checkbox } from "@mui/material";

export const FormikCheckboxList = (props) => {
  const { values, selectedValues, setValues } = props;

  return values.map((eventType) => (
    <div key={eventType.label}>
      <Checkbox
        style={{ marginRight: 8 }}
        checked={selectedValues.indexOf(eventType) > -1}
        onChange={(e) => {
          const newEventTypes = [...selectedValues];
          // if value is All, check or uncheck all
          if (eventType.label === "All") {
            if (e.target.checked) {
              newEventTypes.push(...values);
            } else {
              newEventTypes.splice(0, newEventTypes.length);
            }
          } else {
            // if value is not All, uncheck All
            const index = newEventTypes.findIndex((val) => val.label === "All");
            if (index > -1) {
              newEventTypes.splice(index, 1);
            }

            if (e.target.checked) {
              newEventTypes.push(eventType);
            } else {
              newEventTypes.splice(newEventTypes.indexOf(eventType), 1);
            }
          }
          setValues(newEventTypes);
        }}
      />
      {eventType.label}
    </div>
  ));
};

FormikCheckboxList.propTypes = {
  values: PropTypes.array.isRequired,
  selectedValues: PropTypes.array.isRequired,
  setValues: PropTypes.func.isRequired,
};
