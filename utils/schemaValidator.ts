import Ajv from "ajv";

const ajv = new Ajv({ allErrors: true, strict: false });

export const validateSchema = <T>(schema: object, data: T) => {
  const validate = ajv.compile(schema);
  const valid = validate(data);
  return { valid, errors: validate.errors };
};
