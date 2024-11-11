SELECT
    keycloak_id,
    user_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user,
    COALESCE(
        jsonb_agg(
            jsonb_build_object('org', org_name, 'role', role)
        ) FILTER (WHERE org_name IS NOT NULL AND role IS NOT NULL),
        '[]'::jsonb
    ) AS organizations
FROM (
    SELECT
        users.keycloak_id,
        users.user_id,
        users.email,
        users.first_name,
        users.last_name,
        users.created_timestamp,
        users.super_user,
        org.name AS org_name,
        roles.name AS role
    FROM
        public.users
    LEFT JOIN
        public.user_organization AS uo ON uo.user_id = users.user_id
    LEFT JOIN
        public.organizations AS org ON org.organization_id = uo.organization_id
    LEFT JOIN
        public.roles ON roles.role_id = uo.role_id
) AS subquery
GROUP BY
    user_id,
    keycloak_id,
    email,
    first_name,
    last_name,
    created_timestamp,
    super_user;