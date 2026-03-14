export const adminRecordApi = {
  pageRecords: () => fetch("/api/admin/records"),
  getMatchDetail: (matchCode: string) => fetch(`/api/admin/records/${matchCode}`)
};
