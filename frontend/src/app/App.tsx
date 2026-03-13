import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Provider } from "react-redux";
import { App as AntApp } from "antd";
import { PAGES } from "./router/appRouter";
import { store } from "./store";

const router = createBrowserRouter(PAGES);

function App() {
  return (
    <Provider store={store}>
      <AntApp>
        <RouterProvider router={router} />
      </AntApp>
    </Provider>
  );
}

export default App;
