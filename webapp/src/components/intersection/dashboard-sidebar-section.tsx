import PropTypes from "prop-types";
import { List, ListSubheader } from "@mui/material";
import { DashboardSidebarItem } from "./dashboard-sidebar-item";

const renderNavItems = ({ depth = 0, items, path }) => (
  <List disablePadding>
    {items.reduce((acc, item) => reduceChildRoutes({ acc, depth, item, path }), [])}
  </List>
);

const getPartialMatch = (path, itemPath) => {
  if (path === "/" && itemPath === path) return true;
  else if (itemPath && itemPath !== "/") {
    return path.includes(itemPath);
  }
  return false;
};

const reduceChildRoutes = ({ acc, depth, item, path }) => {
  const key = `${item.title}-${depth}`;
  const partialMatch = getPartialMatch(path, item.path);
  const exactMatch = path.split("?")[0] === item.path; // We don't compare query params

  if (item.children) {
    acc.push(
      <DashboardSidebarItem
        active={partialMatch}
        chip={item.chip}
        depth={depth}
        icon={item.icon}
        info={item.info}
        key={key}
        open={partialMatch}
        path={item.path}
        title={item.title}
      >
        {renderNavItems({
          depth: depth + 1,
          items: item.children,
          path,
        })}
      </DashboardSidebarItem>
    );
  } else {
    acc.push(
      <DashboardSidebarItem
        active={partialMatch}
        chip={item.chip}
        depth={depth}
        icon={item.icon}
        info={item.info}
        key={key}
        path={item.path}
        title={item.title}
      />
    );
  }

  return acc;
};

export const DashboardSidebarSection = (props) => {
  const { items, path, title, ...other } = props;

  return (
    <List
      subheader={
        <ListSubheader
          disableGutters
          disableSticky
          sx={{
            color: "neutral.500",
            fontSize: "0.75rem",
            fontWeight: 700,
            lineHeight: 2.5,
            ml: 4,
            textTransform: "uppercase",
          }}
        >
          {title}
        </ListSubheader>
      }
      {...other}
    >
      {renderNavItems({
        items,
        path,
      })}
    </List>
  );
};

DashboardSidebarSection.propTypes = {
  items: PropTypes.array.isRequired,
  path: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
};
