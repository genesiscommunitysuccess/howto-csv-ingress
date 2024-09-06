import type {GridOptions} from 'ag-grid-community';

export const gridOptions: GridOptions =
  {
    columnDefs: [
      {
        field: "ISO_MIC"
      },
      {
        field: "HOLIDAY_DATE",
      },
      {
        field: "HOLIDAY_TYPE",
      },
      {
        field: "HOLIDAY_NAME",
      },
      {
        field: "HOLIDAY_NAME",
      },
      {
        headerName: "Exchange Name",
        field: "NAME",
      },
      {
        field: "ISO_COUNTRY",
      }
    ],
  }
