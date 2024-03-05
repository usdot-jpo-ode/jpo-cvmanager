type ReduxStateType = {
  user: USER_SLICE_STATE_TYPE;
};

type USER_SLICE_STATE_TYPE = {
  loading: boolean;
  value: {
    authToken?: string;
    parsedJwt?: ParsedJWT;
  };
};
