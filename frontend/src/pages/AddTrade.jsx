// TICKET-ADV123 — React Hook Form + Yup validation.
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { withAuth } from '@components/withAuth.jsx';
import { api } from '@services/apiService.js';

const schema = yup.object({
  tradeRef: yup
    .string()
    .required('Trade ref is required')
    .matches(/^[A-Z]{3}-\d{8}-\d{4}$/, 'Must match AAA-YYYYMMDD-NNNN'),
  instrumentId: yup
    .number()
    .typeError('Instrument id is required')
    .integer()
    .positive()
    .required(),
  counterpartyId: yup
    .number()
    .typeError('Counterparty id is required')
    .integer()
    .positive()
    .required(),
  side: yup
    .string()
    .oneOf(['BUY', 'SELL'], 'Side must be BUY or SELL')
    .required(),
  quantity: yup
    .number()
    .typeError('Quantity is required')
    .positive()
    .required(),
  price: yup
    .number()
    .typeError('Price is required')
    .positive()
    .required(),
  tradeDate: yup
    .date()
    .typeError('Trade date is required')
    .required(),
});

function AddTrade() {
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } =
        useForm({ resolver: yupResolver(schema) });
  const [serverError, setServerError] = useState(null);

  async function onSubmit(values) {
    setServerError(null);
    try {
      await api.createTrade(values);
      reset();
    } catch (err) {
      setServerError(err.message || 'Failed to create trade');
    }
  }

  return (
    <section>
      <h2>Add trade</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="trade-form">
        <label>Trade ref   <input {...register('tradeRef')} placeholder="EQU-20260603-0001" /></label>
        {errors.tradeRef && <p className="form-error">{errors.tradeRef.message}</p>}

        <label>Instrument id   <input {...register('instrumentId')} type="number" /></label>
        {errors.instrumentId && <p className="form-error">{errors.instrumentId.message}</p>}

        <label>Counterparty id   <input {...register('counterpartyId')} type="number" /></label>
        {errors.counterpartyId && <p className="form-error">{errors.counterpartyId.message}</p>}

        <label>
          Side
          <select {...register('side')} defaultValue="">
            <option value="" disabled>Select side</option>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
          </select>
        </label>
        {errors.side && <p className="form-error">{errors.side.message}</p>}

        <label>Quantity   <input {...register('quantity')} type="number" step="any" /></label>
        {errors.quantity && <p className="form-error">{errors.quantity.message}</p>}

        <label>Price   <input {...register('price')} type="number" step="any" /></label>
        {errors.price && <p className="form-error">{errors.price.message}</p>}

        <label>Trade date   <input {...register('tradeDate')} type="date" /></label>
        {errors.tradeDate && <p className="form-error">{errors.tradeDate.message}</p>}

        {serverError && <p role="alert" className="form-error">{serverError}</p>}

        <button disabled={isSubmitting} type="submit">Submit</button>
      </form>
    </section>
  );
}

export default withAuth(AddTrade);
