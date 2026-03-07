import { useState, useEffect, useRef } from "react";
import { Client } from "@stomp/stompjs";
import "../styles/chat.css";

export default function Chat() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [matched, setMatched] = useState(false);
  const [onlineCount, setOnlineCount] = useState(0);
  const [isTyping, setIsTyping] = useState(false); // New state for typing indicator
  const [partnerTyping, setPartnerTyping] = useState(false); // New state for partner typing
  
  const stompClient = useRef(null);
  const typingTimeoutRef = useRef(null); // To handle typing timeout

  const connect = () => {
    const token = localStorage.getItem("googleToken");

    if (!token) {
      console.error("No googleToken found");
      return;
    }

    const backendUrl = import.meta.env.VITE_BACKEND_URL;

    if (!backendUrl) {
      console.error("VITE_BACKEND_URL not defined");
      return;
    }

    const wsUrl = backendUrl.replace(/^http/, "ws");

    const client = new Client({
      brokerURL: `${wsUrl}/ws?token=${token}`,
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: () => {} // disable verbose logs
    });

    client.onConnect = () => {
      console.log("Connected to backend");

      // Subscribe to online count
      client.subscribe("/topic/onlineCount", (msg) => {
        setOnlineCount(parseInt(msg.body));
      });



  // Add this to request current count immediately after connect
  setTimeout(() => {
    // Force a request for current count by publishing to a special endpoint
    client.publish({
      destination: "/app/getOnlineCount"  // We'll add this endpoint
    });
  }, 1000);









      // Subscribe to match events
      client.subscribe("/user/topic/match", () => {
        setMatched(true);
        setPartnerTyping(false); // Reset typing when new match starts
      });

      // Subscribe to left events
      client.subscribe("/user/topic/left", () => {
        setMessages([]);
        setMatched(false);
        setPartnerTyping(false); // Reset typing when partner leaves
      });

      // Subscribe to messages
      client.subscribe("/user/topic/messages", (msg) => {
        const body = JSON.parse(msg.body);
        setMessages(prev => [
          ...prev,
          { type: "received", text: body.content }
        ]);
      });

      // 👇 NEW: Subscribe to typing events
      client.subscribe("/user/topic/typing", (msg) => {
        const event = JSON.parse(msg.body);
        setPartnerTyping(event.typing);
        
        // Auto-hide typing indicator after 3 seconds (backup)
        if (event.typing) {
          setTimeout(() => {
            setPartnerTyping(false);
          }, 3000);
        }
      });

      // Join matchmaking queue
     if (!matched) {
  client.publish({
    destination: "/app/join"
  });
}
    };

    client.onStompError = (frame) => {
      console.error("Broker error:", frame);
    };

    client.activate();
    stompClient.current = client;
  };

  useEffect(() => {
  connect();

  setTimeout(() => {
    if (stompClient.current) {
      stompClient.current.publish({
        destination: "/app/join"
      });
    }
  }, 300);

  return () => {
    if (stompClient.current) {
      stompClient.current.deactivate();
    }
  };
}, []);

  // 👇 NEW: Handle typing events
  const handleTyping = (isTyping) => {
    if (!stompClient.current || !matched) return;

    // Send typing status to partner
    stompClient.current.publish({
      destination: "/app/typing",
      body: JSON.stringify({ 
        typing: isTyping,
        timestamp: Date.now()
      })
    });

    // If typing, set a timeout to auto-stop typing after 3 seconds of inactivity
    if (isTyping) {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
      
      typingTimeoutRef.current = setTimeout(() => {
        // Auto-send typing stopped after 3 seconds
        stompClient.current.publish({
          destination: "/app/typing",
          body: JSON.stringify({ typing: false })
        });
      }, 3000);
    }
  };

  // 👇 NEW: Handle input change with typing events
  const handleInputChange = (e) => {
    const value = e.target.value;
    setInput(value);
    
    // Send typing status
    if (value.length > 0 && !isTyping) {
      setIsTyping(true);
      handleTyping(true);
    } else if (value.length === 0 && isTyping) {
      setIsTyping(false);
      handleTyping(false);
    }
  };

  const nextUser = () => {
    if (!stompClient.current) return;

    setMessages([]);
    setMatched(false);
    setPartnerTyping(false); // Reset typing
    setIsTyping(false); // Reset typing

    stompClient.current.publish({
      destination: "/app/next"
    });
  };

  const sendMessage = () => {
    if (!input.trim() || !matched || !stompClient.current) return;

    // 👇 NEW: Stop typing indicator when sending message
    if (isTyping) {
      setIsTyping(false);
      handleTyping(false);
    }

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

  // 👇 NEW: Handle key events for typing
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // 👇 NEW: Handle input blur (stop typing)
  const handleBlur = () => {
    if (isTyping) {
      setIsTyping(false);
      handleTyping(false);
    }
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
          {/* 👇 NEW: Show typing indicator */}
          {partnerTyping && matched && (
            <span className="typing-indicator">
              {" "}is typing<span className="dots">
                <span>.</span><span>.</span><span>.</span>
              </span>
            </span>
          )}
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
        <button
          className="next-btn"
          onClick={nextUser}
          disabled={!matched}
        >
          Next
        </button>

        <input
          value={input}
          onChange={handleInputChange}
          onKeyDown={handleKeyDown}
          onBlur={handleBlur}
          placeholder="Aa"
          disabled={!matched}
        />

        <button
          className="send-btn"
          onClick={sendMessage}
          disabled={!matched || !input.trim()}
        >
          ➤
        </button>
      </div>
    </div>
  );
}