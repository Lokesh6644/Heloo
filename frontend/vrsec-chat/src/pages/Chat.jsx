import { useState, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import "../styles/chat.css";

export default function Chat() {

  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [matched, setMatched] = useState(false);
  const [onlineCount, setOnlineCount] = useState(0);
  

  const stompClient = useRef(null);

  const connect = () => {

  const token = localStorage.getItem("googleToken");

  if (!token) {
    console.error("No googleToken found");
    return;
  }

  const backendUrl = import.meta.env.VITE_BACKEND_URL;

  const client = new Client({
    brokerURL: `${backendUrl.replace("https", "wss")}/ws?token=${token}`,
    reconnectDelay: 5000,
    onStompError: (frame) => {
      console.error("Broker error:", frame);
    }
  });

  client.onConnect = () => {

    console.log("Connected to production backend");

    // ✅ All subscriptions MUST be here

    client.subscribe("/user/topic/match", () => {
      setMatched(true);
    });

    client.subscribe("/user/topic/left", () => {
      setMessages([]);
      setMatched(false);
    });

    client.subscribe("/user/topic/messages", (msg) => {
      const body = JSON.parse(msg.body);
      setMessages(prev => [
        ...prev,
        { type: "received", text: body.content }
      ]);
    });

    // ✅ online count subscription moved here
    client.subscribe("/topic/onlineCount", (msg) => {
      setOnlineCount(msg.body);
    });

    // join queue
    client.publish({
      destination: "/app/join"
    });
  };

  client.activate();
  stompClient.current = client;
};

  useEffect(() => {
    connect();

    return () => {
      if (stompClient.current) {
        stompClient.current.deactivate();
      }
    };
  }, []);

  const nextUser = () => {

    if (!stompClient.current) return;

    setMessages([]);
    setMatched(false);

    stompClient.current.publish({
      destination: "/app/next"
    });
  };

  const sendMessage = () => {

    if (!input.trim() || !matched || !stompClient.current) return;

    stompClient.current.publish({
      destination: "/app/chat",
      body: JSON.stringify({ content: input })
    });

    setMessages(prev => [
      ...prev,
      { type: "sent", text: input }
    ]);

    setInput("");
  };

  return (
    <div className="chat-container">
    <h4>Online Users: {onlineCount}</h4>
      <div className="chat-header">
        <div>
          <h3>Stranger</h3>
          <span className="online">
            {matched ? "Online" : "Searching..."}
          </span>
        </div>
      </div>

      <div className="chat-body">
        {messages.map((msg, index) => (
          <div key={index} className={`message ${msg.type}`}>
            {msg.text}
          </div>
        ))}
      </div>

      <div className="chat-input">

        <button className="next-btn" onClick={nextUser} disabled={!matched}>
          Next
        </button>

        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              sendMessage();
            }
          }}
          placeholder="Aa"
        />

        <button className="send-btn" onClick={sendMessage}>
          ➤
        </button>

      </div>

    </div>
  );
}