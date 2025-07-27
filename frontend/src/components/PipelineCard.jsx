import React from 'react'
import { useNavigate } from 'react-router-dom';

function PipelineCard({pipeline}) {
  const navigate = useNavigate();
  return (
    <div className='bg-white shadow-md p-4 rounded-xl mb-4' onClick={() => {navigate(`/pipeline/${pipeline.id}`);console.log("clicked")}} >
        <h2 className='text-xl font-bold'>{pipeline.name}</h2>
      
    </div>
  )
}

export default PipelineCard
