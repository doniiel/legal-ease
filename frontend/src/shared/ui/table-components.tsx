/** Editorial table style override — pass as `components` prop to Ant Design Table */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const editorialTableComponents: any = {
  header: {
    cell: ({ children, ...rest }: React.ThHTMLAttributes<HTMLTableCellElement>) => (
      <th
        {...rest}
        style={{
          background: "#f8fafc",
          padding: "16px 32px",
          fontSize: 11,
          fontWeight: 700,
          textTransform: "uppercase",
          letterSpacing: "0.08em",
          color: "#94a3b8",
          borderBottom: "1px solid #f1f5f9",
          whiteSpace: "nowrap",
        }}
      >
        {children}
      </th>
    ),
  },
  body: {
    row: ({ children, ...rest }: React.HTMLAttributes<HTMLTableRowElement>) => (
      <tr
        {...rest}
        style={{ transition: "background 0.15s", cursor: "default" }}
        onMouseEnter={(e) => ((e.currentTarget as HTMLTableRowElement).style.background = "rgba(239,246,255,0.5)")}
        onMouseLeave={(e) => ((e.currentTarget as HTMLTableRowElement).style.background = "transparent")}
      >
        {children}
      </tr>
    ),
    cell: ({ children, ...rest }: React.TdHTMLAttributes<HTMLTableCellElement>) => (
      <td
        {...rest}
        style={{ padding: "18px 32px", borderBottom: "1px solid #f8fafc", verticalAlign: "middle" }}
      >
        {children}
      </td>
    ),
  },
};

export default editorialTableComponents;
