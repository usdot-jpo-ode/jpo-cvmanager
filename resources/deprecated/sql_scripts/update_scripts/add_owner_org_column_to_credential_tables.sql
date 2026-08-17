-- alter rsu_credentials table
ALTER TABLE public.rsu_credentials
    ADD COLUMN owner_organization_id INTEGER,
    ADD CONSTRAINT fk_rsu_credential_owner_organization_id FOREIGN KEY (owner_organization_id)
        REFERENCES public.organizations (organization_id);

UPDATE public.rsu_credentials rc
SET owner_organization_id = (
    SELECT ro.organization_id
    FROM public.rsu_organization ro
    JOIN public.rsus r ON ro.rsu_id = r.rsu_id
    WHERE r.credential_id = rc.credential_id
    ORDER BY r.rsu_id ASC
    LIMIT 1
);



-- alter snmp_credentials table
ALTER TABLE public.snmp_credentials
    ADD COLUMN owner_organization_id INTEGER,
    ADD CONSTRAINT fk_snmp_credential_owner_organization_id FOREIGN KEY (owner_organization_id)
        REFERENCES public.organizations (organization_id);

UPDATE public.snmp_credentials sc
SET owner_organization_id = (
    SELECT ro.organization_id
    FROM public.rsu_organization ro
    JOIN public.rsus r ON ro.rsu_id = r.rsu_id
    WHERE r.snmp_credential_id = sc.snmp_credential_id
    ORDER BY r.rsu_id ASC
    LIMIT 1
);

-- Create orphaned_credentials organization if there are any orphaned records
DO $$
    DECLARE
        orphaned_credentials_org_id INTEGER;
    BEGIN
        -- Check if any orphaned records exist in either rsu_credentials or snmp_credentials
        IF EXISTS (SELECT 1 FROM public.rsu_credentials WHERE owner_organization_id IS NULL) OR
           EXISTS (SELECT 1 FROM public.snmp_credentials WHERE owner_organization_id IS NULL) THEN

            -- Attempt to insert the 'orphaned_credentials' organization.
            -- If it already exists, the ON CONFLICT clause will prevent an error.
            INSERT INTO public.organizations (name)
            VALUES ('orphaned_credentials')
            ON CONFLICT (name) DO NOTHING
            RETURNING organization_id INTO orphaned_credentials_org_id;

            -- If the organization already existed, the RETURNING clause won't set orphaned_credentials_org_id.
            -- In that case, we need to fetch the existing organization's ID.
            IF orphaned_credentials_org_id IS NULL THEN
                SELECT organization_id INTO orphaned_credentials_org_id
                FROM public.organizations
                WHERE name = 'orphaned_credentials';
            END IF;

            -- Update any orphaned rsu_credentials to point to the 'orphaned_credentials' organization
            UPDATE public.rsu_credentials
            SET owner_organization_id = orphaned_credentials_org_id
            WHERE owner_organization_id IS NULL;

            -- Update any orphaned snmp_credentials to point to the 'orphaned_credentials' organization
            UPDATE public.snmp_credentials
            SET owner_organization_id = orphaned_credentials_org_id
            WHERE owner_organization_id IS NULL;
        END IF;
    END $$;

ALTER TABLE public.snmp_credentials
    ALTER COLUMN owner_organization_id SET NOT NULL;

ALTER TABLE public.rsu_credentials
    ALTER COLUMN owner_organization_id SET NOT NULL;